package io.upro.catalog.infrastructure.web;

import io.upro.catalog.application.CatalogService;
import io.upro.catalog.domain.LearningModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
class CatalogController {

    private final CatalogService catalog;

    CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    List<ModuleResponse> list() {
        return catalog.listModules().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{moduleId}")
    ModuleResponse get(@PathVariable String moduleId) {
        return toResponse(catalog.getModule(moduleId));
    }

    private ModuleResponse toResponse(LearningModule module) {
        return new ModuleResponse(module.id(), module.title(), module.description(),
                catalog.questionsOf(module.id()).size());
    }

    record ModuleResponse(String id, String title, String description, int questionCount) {
    }
}
