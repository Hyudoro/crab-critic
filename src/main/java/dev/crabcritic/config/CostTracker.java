package dev.crabcritic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CostTracker, DailyBudget and RateLimitFilter are all three a security for me.
 * you can delete them, modify a little bit the controller, and the application.yml and should be good to go.
 */
@Component
public class CostTracker {

    private static final Logger log = LoggerFactory.getLogger(CostTracker.class);

    // $0.006 per roast
    private static final long COST_PER_ROAST_MICRODOLLARS = 6_000; // $0.006 in microdollars

    private final long budgetMicrodollars;
    private final AtomicLong spentMicrodollars;
    private final Path storageFile;

    public CostTracker(
            @Value("${crab.budget-dollars:2.0}") double budgetDollars
    ) {
        this.budgetMicrodollars = (long) (budgetDollars * 1_000_000);
        this.storageFile = Path.of("crab-budget.txt");
        this.spentMicrodollars = new AtomicLong(loadSpent());

        double spent = spentMicrodollars.get() / 1_000_000.0;
        int roastsLeft = (int) ((budgetMicrodollars - spentMicrodollars.get()) / COST_PER_ROAST_MICRODOLLARS);

        log.info("Crab budget: ${} total | ${} spent | ~{} roasts remaining",
                String.format("%.2f", budgetDollars),
                String.format("%.3f", spent),
                Math.max(0, roastsLeft));
    }

    /**
     * Check if budget allows another roast. If yes, deduct the cost.
     *
     * @return true if the roast is allowed, false if budget is dead
     */
    public boolean trySpend() {
        long current = spentMicrodollars.get();
        if (current + COST_PER_ROAST_MICRODOLLARS > budgetMicrodollars) {
            log.warn("BUDGET EXHAUSTED. Spent: ${}, Limit: ${}",
                    String.format("%.3f", current / 1_000_000.0),
                    String.format("%.2f", budgetMicrodollars / 1_000_000.0));
            return false;
        }

        long newSpent = spentMicrodollars.addAndGet(COST_PER_ROAST_MICRODOLLARS);
        saveSpent(newSpent);

        if (newSpent % 100_000 == 0) { // Log every ~$0.10 spent
            log.info("Budget update: ${} / ${} spent",
                    String.format("%.3f", newSpent / 1_000_000.0),
                    String.format("%.2f", budgetMicrodollars / 1_000_000.0));
        }

        return true;
    }

    public double spentDollars() {
        return spentMicrodollars.get() / 1_000_000.0;
    }

    public double budgetDollars() {
        return budgetMicrodollars / 1_000_000.0;
    }

    public int roastsRemaining() {
        long remaining = budgetMicrodollars - spentMicrodollars.get();
        return (int) Math.max(0, remaining / COST_PER_ROAST_MICRODOLLARS);
    }

    private long loadSpent() {
        try {
            if (Files.exists(storageFile)) {
                String content = Files.readString(storageFile).trim();
                return Long.parseLong(content);
            }
        } catch (IOException | NumberFormatException e) {
            log.warn("Could not read budget file, starting fresh: {}", e.getMessage());
        }
        return 0;
    }

    private void saveSpent(long value) {
        try {
            Files.writeString(storageFile, Long.toString(value));
        } catch (IOException e) {
            log.error("Could not save budget file: {}", e.getMessage());
        }
    }
}