package dev.omnisheetvault.api.catalogue;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CatalogueController {

    private final CatalogueService catalogueService;

    CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/api/catalogue")
    List<CatalogueEntryResponse> list(
            @RequestParam String systemId, @RequestParam(required = false) CatalogueEntryKind kind) {
        return catalogueService.list(systemId, kind);
    }

    @GetMapping("/api/catalogue/sources")
    List<CatalogueSourceResponse> sources(@RequestParam String systemId) {
        return catalogueService.sources(systemId);
    }

    @GetMapping("/api/catalogue/partners")
    List<CataloguePartnerResponse> partners(@RequestParam String systemId) {
        return catalogueService.partners(systemId);
    }

    @GetMapping("/api/catalogue/{id}")
    CatalogueEntryResponse get(@PathVariable UUID id) {
        return catalogueService.get(id);
    }
}
