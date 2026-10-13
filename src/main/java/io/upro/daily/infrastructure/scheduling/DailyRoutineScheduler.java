package io.upro.daily.infrastructure.scheduling;

import io.upro.daily.application.DailyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Declenche le traitement quotidien chaque nuit, et au demarrage pour rattraper
 * les journees manquees (redeploiement, mise en veille...).
 */
public class DailyRoutineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyRoutineScheduler.class);

    private final DailyService daily;

    public DailyRoutineScheduler(DailyService daily) {
        this.daily = daily;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        run();
    }

    @Scheduled(cron = "${upro.daily.cron}", zone = "${upro.zone}")
    public void nightly() {
        run();
    }

    private void run() {
        try {
            daily.runDailyRoutine();
            log.info("Daily routine done");
        } catch (RuntimeException e) {
            log.error("Daily routine failed", e);
        }
    }
}
