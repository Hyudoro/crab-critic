package dev.crabcritic.controller;

import dev.crabcritic.model.RoastRequest;
import dev.crabcritic.model.RoastResponse;
import dev.crabcritic.model.ScrapedContent;
import dev.crabcritic.model.SiteAnalysis;
import dev.crabcritic.service.AnalysisService;
import dev.crabcritic.service.ScraperService;
import dev.crabcritic.service.SvgRenderer;
import dev.crabcritic.config.DailyBudget;
import dev.crabcritic.config.CostTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CrabController {

    private static final Logger log = LoggerFactory.getLogger(CrabController.class);

    private final ScraperService scraper;
    private final AnalysisService analyzer;
    private final SvgRenderer svgRenderer;
    private final DailyBudget dailyBudget;
    private final CostTracker costTracker;

    public CrabController(ScraperService scraper,
                          AnalysisService analyzer,
                          SvgRenderer svgRenderer,
                          DailyBudget dailyBudget,
                          CostTracker costTracker) {
        this.scraper = scraper;
        this.analyzer = analyzer;
        this.svgRenderer = svgRenderer;
        this.dailyBudget = dailyBudget;
        this.costTracker = costTracker;
    }

    @PostMapping("/roast")
    public ResponseEntity<RoastResponse> roast(@RequestBody RoastRequest request) {

        log.info("Roast request: url={}, level={}", request.url(), request.roastLevel());

        // Gate 1: Hard dollar cap, once budget is gone, it's gone permanently
        if (!costTracker.trySpend()) {
            return ResponseEntity.status(429).body(new RoastResponse(
                    request.url(),
                    "Budget exhausted",
                    "retired",
                    java.util.List.of(),
                    java.util.List.of(),
                    "The crab has spent its entire lifetime roast budget.",
                    "I've spent $" + String.format("%.2f", costTracker.spentDollars()) +
                            " of my $" + String.format("%.2f", costTracker.budgetDollars()) +
                            " budget. I'm retired now. Delete crab-budget.txt to revive me.",
                    request.roastLevel(),
                    svgRenderer.render(5, false)
            ));
        }

        // Gate 2: Daily burst limit, resets every day
        if (!dailyBudget.tryConsume()) {
            return ResponseEntity.status(429).body(new RoastResponse(
                    request.url(),
                    "Daily limit reached",
                    "sleeping",
                    java.util.List.of(),
                    java.util.List.of(),
                    "The crab has roasted too many sites today.",
                    "I've used all my roast energy for today. " +
                            "Come back tomorrow or whenever YOU want to pay (" + costTracker.roastsRemaining() +
                            " roasts left in lifetime budget)",
                    request.roastLevel(),
                    svgRenderer.render(2, false)
            ));
        }

        try {
            ScrapedContent scraped = scraper.scrape(request.url());
            SiteAnalysis analysis = analyzer.analyze(scraped, request.roastLevel());
            String svg = svgRenderer.render(analysis.cringeScore(), analysis.isPositive());

            var response = new RoastResponse(
                    request.url(),
                    analysis.product(),
                    analysis.tone(),
                    analysis.buzzwords(),
                    analysis.cliches(),
                    analysis.vibe(),
                    analysis.reaction(),
                    request.roastLevel(),
                    svg
            );

            return ResponseEntity.ok(response);

        } catch (java.io.IOException e) {
            log.error("Scraping/analysis failed for {}: {}", request.url(), e.getMessage());
            return ResponseEntity.badRequest().body(new RoastResponse(
                    request.url(),
                    "Unknown, couldn't reach this site sorry to disappoint you, Im just a crab :(",
                    "unreachable",
                    java.util.List.of(),
                    java.util.List.of("Site didn't respond"),
                    "The crab tried, but the site didn't cooperate.",
                    "I reached out with all eight legs and got nothing back. " +
                            "Either this site is down, or it's hiding from me. Smart.",
                    request.roastLevel(),
                    svgRenderer.render(3, false)
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/budget")
    public ResponseEntity<String> budget() {
        return ResponseEntity.ok(String.format(
                "Spent: $%.3f / $%.2f | %d roasts remaining",
                costTracker.spentDollars(),
                costTracker.budgetDollars(),
                costTracker.roastsRemaining()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("The crab is alive and judgmental.");
    }
}