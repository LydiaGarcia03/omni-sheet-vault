package dev.omnisheetvault.api.catalogue;

import jakarta.validation.ConstraintViolation;
import java.nio.file.Path;
import java.util.Set;

/** Thrown by the import command only — never reaches an HTTP response. */
public class CatalogueImportException extends RuntimeException {

    public CatalogueImportException(Path path, Throwable cause) {
        super("Failed to import catalogue content from " + path, cause);
    }

    public CatalogueImportException(Path file, Set<ConstraintViolation<CatalogueEntryImport>> violations) {
        super("Invalid catalogue entry in " + file + ": " + describe(violations));
    }

    private static String describe(Set<ConstraintViolation<CatalogueEntryImport>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}
