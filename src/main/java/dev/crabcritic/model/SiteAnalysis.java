package dev.crabcritic.model;

import java.util.List;

/**
 * What Claude returns after analyzing the scraped content.
 * Mapped directly from Claude's JSON response.
 *
 * isPositive: true if the crab genuinely approves (rare).
 * cringeScore: 1-5, drives the crab's facial expression independently of roastLevel.
 *   roastLevel = how mean the TEXT is.
 *   cringeScore = how distressed the CRAB looks.
 *   A polite roast (level 1) of a terrible site still shows a distressed crab (cringeScore 5).
 */
public record SiteAnalysis(
        String product,
        String tone,
        List<String> buzzwords,
        List<String> cliches,
        String vibe,
        String reaction,
        int cringeScore,
        boolean isPositive
) {}
