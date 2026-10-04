package dev.omnisheetvault.api.catalogue;

import java.util.List;

/** One partner brand and its imported source books. */
public record CataloguePartnerResponse(String name, List<String> sourceBooks) {
}
