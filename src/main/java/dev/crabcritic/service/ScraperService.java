package dev.crabcritic.service;

import dev.crabcritic.model.ScrapedContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    // your money will thank you. the joke might get you broke otherwise, (You maybe already are).
    private static final int BODY_TEXT_LIMIT = 1500;

    // jsoup timeout in millis. If a landing page takes longer than this, it deserves the roast. hahah this is the one I'm the most proud of.
    private static final int TIMEOUT_MS = 8_000;

    /**
     * Scrapes a URL and extracts the parts that matter for landing page analysis.
     *
     * Why jsoup and not Selenium or whatever?
     *  Zero external dependencies.
     *  Handles 80% of landing pages.
     *  milliseconds, not seconds.
     *
     * JS rendered content will  be sloppy tho and I don't care.
     */
    public ScrapedContent scrape(String url) throws IOException {
        // Normalize: if someone types "example.com" without protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        log.info("Scraping: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("CrabCritic/0.1 (landing-page-humor-bot)")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .get();

        String title = doc.title();

        // These are the bold claims.
        List<String> headings = doc.select("h1, h2, h3").stream()
                .map(Element::text)
                .filter(t -> !t.isBlank())
                .limit(15) // No site needs more than 15 headings analyzed
                .toList();

        // CTA buttons , <button> and <a> that look like buttons.
        // These reveal what the site WANTS from you. (could have added more but it quickly get
        // messy and if you are reading this anyway you probably implemented it yourself)
        List<String> ctas = doc.select("button, a.btn, a.button, [class*=cta], [class*=CTA], a[href*=signup], a[href*=demo], a[href*=trial]").stream()
                .map(Element::text)
                .filter(t -> !t.isBlank() && t.length() < 80)
                .distinct()
                .limit(10)
                .toList();

        String metaDesc = "";
        Element metaEl = doc.selectFirst("meta[name=description]");
        if (metaEl != null) {
            metaDesc = metaEl.attr("content");
        }

        // strip all HTML, take first N chars.
        // This gives Claude the content to judge.
        String fullText = doc.body().text();
        String bodyPreview = fullText.length() > BODY_TEXT_LIMIT
                ? fullText.substring(0, BODY_TEXT_LIMIT) + "..."
                : fullText;

        var result = new ScrapedContent(title, headings, ctas, metaDesc, bodyPreview);

        log.info("Scraped: title='{}', {} headings, {} CTAs, {} chars body",
                title, headings.size(), ctas.size(), bodyPreview.length());

        return result;
    }
}
