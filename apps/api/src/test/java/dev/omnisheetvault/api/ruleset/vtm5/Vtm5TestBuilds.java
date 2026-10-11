package dev.omnisheetvault.api.ruleset.vtm5;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** A complete, rule-abiding VtM build: a Toreador neonate Consensualist, and helpers to vary it. */
final class Vtm5TestBuilds {

    static final ObjectMapper MAPPER = new ObjectMapper();
    static final Vtm5Content CONTENT = new Vtm5Content(MAPPER);

    static final String VALID = """
            {"concept":"Gallery restorer","chronicle":"Lisbon by Night","sire":"Isadora","age":"Neonate","generation":"13th",
             "clan":"Toreador",
             "attributes":{"Intelligence":4,"Dexterity":3,"Charisma":3,"Wits":3,"Strength":2,"Stamina":2,"Manipulation":2,
                           "Resolve":2,"Composure":1},
             "distribution":"Balanced",
             "skills":{"Academics":3,"Persuasion":3,"Insight":3,
                       "Etiquette":2,"Awareness":2,"Investigation":2,"Occult":2,"Larceny":2,
                       "Athletics":1,"Drive":1,"Medicine":1,"Politics":1,"Finance":1,"Stealth":1,"Science":1},
             "specialties":{"Academics":"Art history","Science":"Chemistry","Persuasion":"Victims"},
             "disciplineTwo":"Auspex","disciplineOne":"Celerity",
             "powers":{"Auspex":["Heightened Senses","Sense the Unseen","Premonition"],"Celerity":["Cat's Grace"]},
             "predator":"Consensualist","predatorSpecialty":0,"predatorDiscipline":"Auspex",
             "advantages":{"Herd":2,"Resources":2,"Beautiful":2,"Contacts":1,"Prey Exclusion":1,"Dark Secret":1},
             "convictions":[{"conviction":"Never destroy a work of art.","touchstone":"Léa"}],
             "ambition":"A seat at court","desire":""}
            """;

    private Vtm5TestBuilds() {
    }

    static ObjectNode valid() {
        return (ObjectNode) MAPPER.readTree(VALID);
    }

    static String json(ObjectNode build) {
        return MAPPER.writeValueAsString(build);
    }

    /** The sheet the valid build materializes into. */
    static ObjectNode validSheet() {
        Vtm5CharacterCreationFlow flow = new Vtm5CharacterCreationFlow(MAPPER, CONTENT);
        return (ObjectNode) MAPPER.readTree(flow.materialize(null, VALID, null).sheetJson());
    }
}
