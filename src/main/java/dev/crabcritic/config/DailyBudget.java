package dev.crabcritic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**

 * If a tweet goes viral, 500 people × 3 roasts = $7.50 in one afternoon.
 * This cap prevents that. When the budget is exhausted, the API returns
 * a friendly "crab is sleeping" message instead of burning my money.
 *
 * Default: 100 roasts/day = ~$0.50/day max.
 * Adjust via application.yml: crab.daily-limit
 */
@Component
public class DailyBudget {

    private static final Logger log = LoggerFactory.getLogger(DailyBudget.class);

    private final int dailyLimit;
    private final AtomicInteger todayCount = new AtomicInteger(0);
    private final AtomicReference<LocalDate> currentDay = new AtomicReference<>(LocalDate.now());

    public DailyBudget(@Value("${crab.daily-limit:100}") int dailyLimit) {
        this.dailyLimit = dailyLimit;
        log.info("Daily roast budget: {} roasts/day (~${} max/day)",
                dailyLimit, String.format("%.2f", dailyLimit * 0.005));
    }

    /**
     * Try to consume one roast from today's budget.
     *
     * @return true if allowed, false if budget exhausted
     */
    public boolean tryConsume() {
        // Reset counter at midnight
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDay.get())) {
            currentDay.set(today);
            todayCount.set(0);
            log.info("New day, budget reset. {} roasts available.", dailyLimit);
        }

        int current = todayCount.incrementAndGet();
        if (current > dailyLimit) {
            log.warn("Daily budget exhausted ({}/{})", current, dailyLimit);
            return false;
        }

        log.debug("Roast {}/{} today", current, dailyLimit);
        return true;
    }

    public int remaining() {
        return Math.max(0, dailyLimit - todayCount.get());
    }
}