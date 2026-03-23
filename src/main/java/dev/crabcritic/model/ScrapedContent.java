package dev.crabcritic.model;

import java.util.List;

/**
 * Raw content scraped from a landing page.
 * This is what we feed to Claude for analysis.
 * 
 * We extract only what matters for judging a landing page:
 * headings (what they claim), CTAs (what they want from you),
 * meta description (what they tell Google), and body text (the filler).
 */
public record ScrapedContent(
        String title,
        List<String> headings,
        List<String> ctaButtons,
        String metaDescription,
        String bodyTextPreview // first ~1000 chars of visible text
) {}
