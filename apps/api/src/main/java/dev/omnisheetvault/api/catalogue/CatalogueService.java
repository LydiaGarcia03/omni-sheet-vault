package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.registry.GameSystemRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/**
 * Reference data: no ownership check, unlike {@code CharacterService} — every
 * authenticated player reads the same catalogue. Redaction is applied here, at the
 * one seam every read passes through, from a single configured switch — see adr-0005.
 */
@Service
public class CatalogueService {

    private final CatalogueEntryRepository catalogueEntryRepository;
    private final GameSystemRegistry gameSystemRegistry;
    private final ObjectMapper objectMapper;
    private final boolean redactionEnabled;

    public CatalogueService(
            CatalogueEntryRepository catalogueEntryRepository,
            GameSystemRegistry gameSystemRegistry,
            ObjectMapper objectMapper,
            @Value("${app.catalogue.redaction-enabled}") boolean redactionEnabled) {
        this.catalogueEntryRepository = catalogueEntryRepository;
        this.gameSystemRegistry = gameSystemRegistry;
        this.objectMapper = objectMapper;
        this.redactionEnabled = redactionEnabled;
    }

    public List<CatalogueEntryResponse> list(String systemId, CatalogueEntryKind kind) {
        gameSystemRegistry.forSystem(systemId);
        List<CatalogueEntry> entries = kind == null
                ? catalogueEntryRepository.findBySystemId(systemId)
                : catalogueEntryRepository.findBySystemIdAndKind(systemId, kind);
        return entries.stream().map(this::toResponse).toList();
    }

    /** The imported source books, how many entries each holds, and which are playtest or partnered, for a builder's source toggles. */
    public List<CatalogueSourceResponse> sources(String systemId) {
        gameSystemRegistry.forSystem(systemId);
        return catalogueEntryRepository.countBySourceBook(systemId).stream()
                .map(row -> new CatalogueSourceResponse(row.getSourceBook(), row.getSourceCode(), row.getEntryCount(),
                        PlaytestSources.isPlaytest(row.getSourceCode()), PartneredSources.partnerOf(row.getSourceCode())))
                .toList();
    }

    /** The partner brands with imported source books, in D&D Beyond's order, for a builder's partner picker. */
    public List<CataloguePartnerResponse> partners(String systemId) {
        List<CatalogueSourceResponse> sources = sources(systemId);
        return PartneredSources.partners().stream()
                .map(partner -> new CataloguePartnerResponse(partner, sources.stream()
                        .filter(source -> partner.equals(source.partner()))
                        .map(CatalogueSourceResponse::sourceBook)
                        .sorted()
                        .toList()))
                .filter(partner -> !partner.sourceBooks().isEmpty())
                .toList();
    }

    public CatalogueEntryResponse get(UUID id) {
        CatalogueEntry entry = catalogueEntryRepository.findById(id).orElseThrow(() -> new CatalogueEntryNotFoundException(id));
        return toResponse(entry);
    }

    /**
     * For a cross-feature caller that already knows the exact natural key it wants,
     * e.g. a build's starting items. Never redacted, same reasoning as
     * {@link #getUnredacted}: the caller is about to copy this content into a
     * character's own sheet, not browse the catalogue.
     */
    public CatalogueEntry findBySlug(String systemId, CatalogueEntryKind kind, String slug) {
        return catalogueEntryRepository.findBySystemIdAndKindAndSlug(systemId, kind, slug)
                .orElseThrow(() -> new CatalogueEntryNotFoundException(systemId, kind, slug));
    }

    /**
     * For a cross-feature caller copying catalogue content into a player's own
     * character (phase 9's "Manage spells" learning a spell) — never redacted,
     * regardless of the configured switch. adr-0005: redaction "reduces what a
     * visitor reads; it does not change what the repository holds," and a
     * player's own copy of a spell they learned is no longer catalogue browsing,
     * the same way {@code CharacterSheetService} never redacts a character's own
     * stored data.
     */
    public CatalogueEntryResponse getUnredacted(UUID id) {
        CatalogueEntry entry = catalogueEntryRepository.findById(id).orElseThrow(() -> new CatalogueEntryNotFoundException(id));
        return CatalogueEntryResponse.from(entry, false, objectMapper);
    }

    /** An entry's raw {@code data} JSON by natural key, or empty when the catalogue has no such entry; never redacted. */
    public Optional<String> findData(String systemId, CatalogueEntryKind kind, String slug) {
        return catalogueEntryRepository.findBySystemIdAndKindAndSlug(systemId, kind, slug).map(CatalogueEntry::data);
    }

    /** The imported catalogue as the rules engine reads it, for materializing builds; never redacted. */
    public CatalogueLookup lookup(String systemId) {
        gameSystemRegistry.forSystem(systemId);
        return new DatabaseCatalogueLookup(catalogueEntryRepository, systemId, objectMapper, redactionEnabled);
    }

    private CatalogueEntryResponse toResponse(CatalogueEntry entry) {
        return CatalogueEntryResponse.from(entry, redactionEnabled, objectMapper);
    }
}
