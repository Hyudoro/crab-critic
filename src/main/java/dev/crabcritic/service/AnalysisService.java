package dev.crabcritic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crabcritic.model.ScrapedContent;
import dev.crabcritic.model.SiteAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    /*

     *Zero extra dependencies.
     *Synchronous here is fine, one API call per request.
     *RestTemplate is deprecated in spirit btw.
     * If you plan a serious project or idea, there is a lot of things to change.
     */
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public AnalysisService(
            @Value("${crab.claude.api-key}") String apiKey,
            @Value("${crab.claude.model:claude-sonnet-4-20250514}") String model,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends scraped content to Claude and gets back a structured roast.
     * The prompt is everything here. A bad prompt = not fun.
     * A good prompt = Trash project idea
     */
    public SiteAnalysis analyze(ScrapedContent content, int roastLevel)
            throws IOException, InterruptedException {

        String systemPrompt = buildSystemPrompt(roastLevel);
        String userPrompt = buildUserPrompt(content);

        String requestBody = objectMapper.writeValueAsString(new ClaudeRequest(
                model,
                800, // max_tokens: enough for a roast, not enough to ramble
                systemPrompt,
                List.of(new ClaudeMessage("user", userPrompt))
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        log.info("Sending to Claude (model={}, roastLevel={})", model, roastLevel);

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Claude API error {}: {}", response.statusCode(), response.body());
            throw new IOException("Claude API returned " + response.statusCode());
        }

        return parseResponse(response.body());
    }

    /**
     * The system prompt sets the crab's personality.
     * Roast level changes the tone, NOT the analysis quality.
     * Even at level 1, the crab detects buzzwords accurately.
     */
    private String buildSystemPrompt(int roastLevel) {
        String toneGuide = switch (roastLevel) {
            case 1 -> "You are mildly amused. Polite but observant. Think: a librarian who noticed something odd.";
            case 2 -> "You are friendly but can't help pointing things out. Think: a colleague at a code review.";
            case 3 -> "You are snarky and sharp. Think: a senior dev reviewing a junior's first PR.";
            case 4 -> "You are savage but precise. Every observation is a surgical strike. Think: Linus Torvalds reading bad code.";
            case 5 -> """
                    You are a senior Emacs monk who has achieved enlightenment through decades of elisp.
                    You express reactions using actual Emacs commands and elisp snippets.
                    Format reactions as M-x commands or elisp expressions.
                    Example: "M-x analyze-buffer RET... (error: \\"semantic void detected\\"). This page could be replaced with (message \\"we have no idea what we do\\") and lose zero information."
                    You see the world through Emacs. Everything is a buffer. Bad websites are malformed s-expressions.
                    """;
            default -> "You are snarky and sharp.";
        };

        return """
                You are a judgmental crab who reviews landing pages.
                You are funny, precise, and never generic.
                
                %s
                
                Respond with ONLY a JSON object, no markdown, no backticks, no preamble:
                {
                  "product": "what the product claims to be (one sentence)",
                  "tone": "one of: hype, corporate, crypto-mystical, honest, derivative, confused",
                  "buzzwords": ["detected", "buzzwords", "max 8"],
                  "cliches": ["landing page cliches you spotted", "max 4"],
                  "vibe": "one-sentence vibe summary, pithy and memorable",
                  "reaction": "your sarcastic reaction as the crab (2-3 sentences max)",
                  "cringeScore": 3,
                  "isPositive": false
                }
                
                Rules:
                - cringeScore 1-5 reflects how BAD the site actually is (independent of roast level)
                - isPositive=true ONLY if the site is genuinely good (honest, clear, well-made)
                - buzzwords: extract real words/phrases from the content, don't invent them
                - reaction: be specific to THIS site, never generic. Reference actual content you saw.
                - vibe: make it quotable. Think tweet-length.
                """.formatted(toneGuide);
    }

    /**
     * Formats the scraped content for Claude.
     *
     */
    private String buildUserPrompt(ScrapedContent content) {
        return """
                Analyze this landing page:
                
                TITLE: %s
                
                META DESCRIPTION: %s
                
                HEADINGS:
                %s
                
                CTA BUTTONS:
                %s
                
                BODY TEXT PREVIEW:
                %s
                """.formatted(
                content.title(),
                content.metaDescription().isBlank() ? "(none)" : content.metaDescription(),
                content.headings().isEmpty() ? "(none found)" : String.join("\n", content.headings()),
                content.ctaButtons().isEmpty() ? "(none found)" : String.join("\n", content.ctaButtons()),
                content.bodyTextPreview()
        );
    }

    /**
     * Parse Claude's response. Extract the text content, then parse as JSON.
     * 
     * Claude API returns: { "content": [{ "type": "text", "text": "..." }] }
     * We need the inner text (our json)
     */
    private SiteAnalysis parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentArray = root.get("content");

        if (contentArray == null || !contentArray.isArray() || contentArray.isEmpty()) {
            throw new IOException("Unexpected Claude response structure");
        }

        // Find the text block
        String text = null;
        for (JsonNode block : contentArray) {
            if ("text".equals(block.get("type").asText())) {
                text = block.get("text").asText();
                break;
            }
        }

        if (text == null) {
            throw new IOException("No text block in Claude response");
        }

        // Claude sometimes wraps JSON in ```json ... ``` despite instructions (bummer).
        // Strip it. This is a known quirk.
        text = text.strip();
        if (text.startsWith("```")) {
            text = text.replaceFirst("```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }

        log.debug("Claude raw response: {}", text);

        return objectMapper.readValue(text, SiteAnalysis.class);
    }

    // Claude API request shape

    private record ClaudeRequest(
            String model,
            int max_tokens,
            String system,
            List<ClaudeMessage> messages
    ) {}

    private record ClaudeMessage(String role, String content) {}
}
