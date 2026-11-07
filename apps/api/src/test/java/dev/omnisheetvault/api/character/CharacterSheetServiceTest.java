package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.omnisheetvault.api.catalogue.CatalogueEntryKind;
import dev.omnisheetvault.api.catalogue.CatalogueEntryResponse;
import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.catalogue.RedactableText;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.InvalidExtraException;
import dev.omnisheetvault.api.ruleset.SheetCalculator;
import dev.omnisheetvault.api.ruleset.SheetMutator;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
import dev.omnisheetvault.api.ruleset.registry.MechanicResolverRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetMutatorRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** The sheet orchestration around the catalogue and the build, with the ruleset seams mocked. */
class CharacterSheetServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SYSTEM = "dnd-5e";
    private static final String SHEET = "{\"sheet\":true}";
    private static final String UPDATED_SHEET = "{\"updated\":true}";
    private static final String BUILD = "{\"build\":true}";

    private final CharacterService characterService = mock(CharacterService.class);
    private final CatalogueService catalogueService = mock(CatalogueService.class);
    private final SheetMutator mutator = mock(SheetMutator.class);
    private final SheetCalculator calculator = mock(SheetCalculator.class);
    private final CharacterCreationFlow flow = mock(CharacterCreationFlow.class);
    private final CatalogueLookup lookup = mock(CatalogueLookup.class);
    private final Jwt jwt = mock(Jwt.class);
    private final UUID entryId = UUID.randomUUID();
    private final Character character = Character.create(UUID.randomUUID(), "Aria", SYSTEM);
    private CharacterSheetService service;

    @BeforeEach
    void wireTheSeams() {
        SheetMutatorRegistry mutators = mock(SheetMutatorRegistry.class);
        SheetCalculatorRegistry calculators = mock(SheetCalculatorRegistry.class);
        CharacterCreationFlowRegistry flows = mock(CharacterCreationFlowRegistry.class);
        when(mutators.forSystem(SYSTEM)).thenReturn(mutator);
        when(calculators.forSystem(SYSTEM)).thenReturn(calculator);
        when(flows.forSystem(SYSTEM)).thenReturn(flow);
        when(catalogueService.lookup(SYSTEM)).thenReturn(lookup);
        when(characterService.getMineActive(jwt, character.id())).thenReturn(character);
        character.replaceSheet(SHEET);
        service = new CharacterSheetService(characterService, calculators, mutators, mock(MechanicResolverRegistry.class),
                mock(RollService.class), catalogueService, flows, MAPPER);
    }

    @Test
    void addsACatalogueCreatureAsAnExtra() {
        CatalogueEntryResponse wolf = entry(CatalogueEntryKind.CREATURE, "wolf", "Wolf", "A wolf.");
        when(catalogueService.getUnredacted(entryId)).thenReturn(wolf);
        when(mutator.addCatalogueExtra(SHEET, "Wolf", "MM", "COMPANION", wolf.data(), 2)).thenReturn(UPDATED_SHEET);

        service.addCatalogueExtra(jwt, character.id(), entryId, "COMPANION", 2);

        assertThat(character.sheet()).isEqualTo(UPDATED_SHEET);
        verify(characterService).save(character);
        verify(calculator).calculateVitals(UPDATED_SHEET);
    }

    @Test
    void refusesToAddAnEntryThatIsNotACreature() {
        when(catalogueService.getUnredacted(entryId)).thenReturn(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert."));
        UUID characterId = character.id();

        assertThatThrownBy(() -> service.addCatalogueExtra(jwt, characterId, entryId, "COMPANION", 1))
                .isInstanceOf(InvalidExtraException.class)
                .hasMessage("Not a creature: Alert");
        verify(characterService, never()).save(any());
    }

    @Test
    void updatesAnExtraThroughTheMutator() {
        when(mutator.updateExtra(SHEET, "extra-1", "Rex", "Loyal")).thenReturn(UPDATED_SHEET);

        service.updateExtra(jwt, character.id(), "extra-1", "Rex", "Loyal");

        assertThat(character.sheet()).isEqualTo(UPDATED_SHEET);
        verify(characterService).save(character);
    }

    @Test
    void aCharacterWithoutABuildLearnsAnyFeatWithItsFirstSentenceAsTheSummary() {
        when(catalogueService.getUnredacted(entryId)).thenReturn(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert. Always."));
        when(mutator.learnFeat(eq(SHEET), any())).thenReturn(UPDATED_SHEET);

        service.learnFeat(jwt, character.id(), entryId);

        ArgumentCaptor<FeatureTrait> learned = ArgumentCaptor.forClass(FeatureTrait.class);
        verify(mutator).learnFeat(eq(SHEET), learned.capture());
        assertThat(learned.getValue().name()).isEqualTo("Alert");
        assertThat(character.sheet()).isEqualTo(UPDATED_SHEET);
        verify(catalogueService, never()).lookup(anyString());
    }

    @Test
    void aFeatTheBuildRejectsIsRefusedWithEveryProblem() {
        CatalogueRecord feat = catalogueRecord("alert");
        givenABuild();
        when(catalogueService.getUnredacted(entryId)).thenReturn(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert."));
        when(lookup.find("FEAT", "alert")).thenReturn(Optional.of(feat));
        when(flow.featProblems(BUILD, feat, lookup)).thenReturn(List.of("Needs Dexterity 13", "Already known"));
        UUID characterId = character.id();

        assertThatThrownBy(() -> service.learnFeat(jwt, characterId, entryId))
                .isInstanceOf(FeatNotAllowedException.class)
                .hasMessage("Needs Dexterity 13; Already known");
        verify(mutator, never()).learnFeat(anyString(), any());
    }

    @Test
    void aFeatTheBuildAcceptsIsLearned() {
        CatalogueRecord feat = catalogueRecord("alert");
        givenABuild();
        when(catalogueService.getUnredacted(entryId)).thenReturn(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert."));
        when(lookup.find("FEAT", "alert")).thenReturn(Optional.of(feat));
        when(mutator.learnFeat(eq(SHEET), any())).thenReturn(UPDATED_SHEET);

        service.learnFeat(jwt, character.id(), entryId);

        verify(flow).featProblems(BUILD, feat, lookup);
        assertThat(character.sheet()).isEqualTo(UPDATED_SHEET);
    }

    @Test
    void aFeatMissingFromTheCatalogueLookupIsNotCheckedAgainstTheBuild() {
        givenABuild();
        when(catalogueService.getUnredacted(entryId)).thenReturn(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert."));
        when(lookup.find("FEAT", "alert")).thenReturn(Optional.empty());
        when(mutator.learnFeat(eq(SHEET), any())).thenReturn(UPDATED_SHEET);

        service.learnFeat(jwt, character.id(), entryId);

        verify(flow, never()).featProblems(anyString(), any(), any());
        assertThat(character.sheet()).isEqualTo(UPDATED_SHEET);
    }

    @Test
    void aCharacterWithoutABuildSeesTheWholeCatalogue() {
        List<CatalogueEntryResponse> entries = List.of(entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert."));
        when(catalogueService.list(SYSTEM, CatalogueEntryKind.FEAT)).thenReturn(entries);

        assertThat(service.catalogueFor(jwt, character.id(), CatalogueEntryKind.FEAT)).isEqualTo(entries);
        verify(catalogueService, never()).lookup(anyString());
    }

    @Test
    void aCharacterWithABuildSeesOnlyTheEntriesItsBuildOffers() {
        CatalogueEntryResponse alert = entry(CatalogueEntryKind.FEAT, "alert", "Alert", "Stay alert.");
        CatalogueEntryResponse lucky = entry(CatalogueEntryKind.FEAT, "lucky", "Lucky", "Be lucky.");
        CatalogueRecord offered = catalogueRecord("alert");
        CatalogueRecord hidden = catalogueRecord("lucky");
        givenABuild();
        when(catalogueService.list(SYSTEM, CatalogueEntryKind.FEAT)).thenReturn(List.of(alert, lucky));
        when(lookup.list("FEAT")).thenReturn(List.of(offered, hidden));
        when(flow.offersCatalogueEntry(BUILD, offered)).thenReturn(true);
        when(flow.offersCatalogueEntry(BUILD, hidden)).thenReturn(false);

        assertThat(service.catalogueFor(jwt, character.id(), CatalogueEntryKind.FEAT)).containsExactly(alert);
    }

    private void givenABuild() {
        when(flow.buildOf(SHEET)).thenReturn(Optional.of(BUILD));
    }

    private static CatalogueEntryResponse entry(CatalogueEntryKind kind, String slug, String name, String description) {
        JsonNode data = MAPPER.createObjectNode();
        return new CatalogueEntryResponse(UUID.randomUUID().toString(), SYSTEM, kind, slug, name, "MM", 1, List.of(),
                new RedactableText(description, false), data);
    }

    private static CatalogueRecord catalogueRecord(String slug) {
        return new CatalogueRecord("FEAT", slug, slug, "PHB", 1, "", MAPPER.createObjectNode());
    }
}
