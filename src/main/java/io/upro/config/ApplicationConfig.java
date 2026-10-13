package io.upro.config;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.application.port.CatalogRepository;
import io.upro.catalog.infrastructure.yaml.YamlCatalogRepository;
import io.upro.practice.application.PracticeService;
import io.upro.practice.application.port.ReviewCardRepository;
import io.upro.practice.infrastructure.memory.InMemoryReviewCardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition root : seul endroit ou l'on choisit quels adapters brancher sur les ports.
 */
@Configuration(proxyBeanMethods = false)
class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    CatalogRepository catalogRepository(@Value("${upro.content.location:classpath:content/*.yml}") String location) {
        return new YamlCatalogRepository(location);
    }

    @Bean
    CatalogService catalogService(CatalogRepository catalogRepository) {
        return new CatalogService(catalogRepository);
    }

    @Bean
    ReviewCardRepository reviewCardRepository() {
        return new InMemoryReviewCardRepository();
    }

    @Bean
    PracticeService practiceService(CatalogService catalogService, ReviewCardRepository cards, Clock clock) {
        return new PracticeService(catalogService, cards, clock);
    }
}
