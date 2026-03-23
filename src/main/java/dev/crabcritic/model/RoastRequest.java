package dev.crabcritic.model;

import java.util.List;

/**
 * What the user sends us.
 * That's a URL and how hard they want the crab to go.
 */
public record RoastRequest(
        String url,
        int roastLevel

) {
    public RoastRequest {
        if (roastLevel < 1 || roastLevel > 5) {
            roastLevel = 3;
        }
    }
}
