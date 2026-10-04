package dev.omnisheetvault.api.catalogue;

import java.util.UUID;

public class CatalogueEntryNotFoundException extends RuntimeException {

    public CatalogueEntryNotFoundException(UUID id) {
        super("No catalogue entry with id " + id);
    }

    public CatalogueEntryNotFoundException(String systemId, CatalogueEntryKind kind, String slug) {
        super("No catalogue entry for " + systemId + "/" + kind + "/" + slug);
    }
}
