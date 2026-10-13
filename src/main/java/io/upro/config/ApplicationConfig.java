package io.upro.config;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.infrastructure.yaml.YamlCatalogRepository;
import io.upro.daily.application.DailyService;
import io.upro.daily.infrastructure.jdbc.JdbcDailyQuizRepository;
import io.upro.daily.infrastructure.jdbc.JdbcDailySnapshotRepository;
import io.upro.daily.infrastructure.scheduling.DailyRoutineScheduler;
import io.upro.practice.application.PracticeService;
import io.upro.practice.infrastructure.jdbc.JdbcAnswerLog;
import io.upro.practice.infrastructure.jdbc.JdbcReviewCardRepository;
import io.upro.practice.infrastructure.jdbc.JdbcSkillRatingRepository;
import io.upro.shared.application.Transactions;
import io.upro.shared.infrastructure.SpringTransactions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Composition root : seul endroit ou l'on choisit quels adapters brancher sur les ports.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class ApplicationConfig {

    /** Les journees (quiz du jour, bilans) sont decoupees dans ce fuseau, pas en UTC. */
    @Bean
    Clock clock(@Value("${upro.zone}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    @Bean
    Transactions transactions(PlatformTransactionManager transactionManager) {
        return new SpringTransactions(new TransactionTemplate(transactionManager));
    }

    @Bean
    CatalogRepository catalogRepository(@Value("${upro.content.location}") String location) {
        return new YamlCatalogRepository(location);
    }

    @Bean
    CatalogService catalogService(CatalogRepository catalogRepository) {
        return new CatalogService(catalogRepository);
    }

    @Bean
    PracticeService practiceService(CatalogService catalogService, JdbcClient jdbc, Transactions transactions, Clock clock) {
        return new PracticeService(catalogService, new JdbcReviewCardRepository(jdbc), new JdbcSkillRatingRepository(jdbc),
                new JdbcAnswerLog(jdbc), transactions, clock);
    }

    @Bean
    DailyService dailyService(CatalogService catalogService, PracticeService practiceService, JdbcClient jdbc,
                              Transactions transactions, Clock clock, @Value("${upro.daily.size}") int quizSize) {
        return new DailyService(catalogService, practiceService, new JdbcAnswerLog(jdbc),
                new JdbcDailyQuizRepository(jdbc), new JdbcDailySnapshotRepository(jdbc), transactions, clock, quizSize);
    }

    @Bean
    DailyRoutineScheduler dailyRoutineScheduler(DailyService dailyService) {
        return new DailyRoutineScheduler(dailyService);
    }
}
