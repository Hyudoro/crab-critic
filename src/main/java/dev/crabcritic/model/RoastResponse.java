package dev.crabcritic.model;

import java.util.List;

/**
 * What the crab thinks about your website.
 * This goes straight to the frontend as JSON.
 */
public record RoastResponse(
        String url,
        String product,
        String tone,
        List<String> buzzwords,
        List<String> cliches,
        String vibe,
        String reaction,
        int roastLevel,
        String svg
) {}
