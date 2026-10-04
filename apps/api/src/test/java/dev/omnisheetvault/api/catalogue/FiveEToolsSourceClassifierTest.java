package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class FiveEToolsSourceClassifierTest {

    private final FiveEToolsSourceClassifier classifier = new FiveEToolsSourceClassifier(Set.of("XPHB", "XDMG", "FRHoF"));

    @Test
    void includesAnOrdinarySourceBook() {
        assertThat(classifier.isInScope("PHB")).isTrue();
        assertThat(classifier.isInScope("XGE")).isTrue();
    }

    @Test
    void excludesAnOutOfScopeSourceRegardlessOfCase() {
        assertThat(classifier.isInScope("XPHB")).isFalse();
        assertThat(classifier.isInScope("xdmg")).isFalse();
        assertThat(classifier.isInScope("frhof")).isFalse();
    }
}
