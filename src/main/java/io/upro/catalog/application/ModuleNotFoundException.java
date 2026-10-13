package io.upro.catalog.application;

public class ModuleNotFoundException extends RuntimeException {

    public ModuleNotFoundException(String moduleId) {
        super("Unknown module: " + moduleId);
    }
}
