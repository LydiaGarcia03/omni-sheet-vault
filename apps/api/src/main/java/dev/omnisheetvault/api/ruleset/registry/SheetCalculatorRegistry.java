package dev.omnisheetvault.api.ruleset.registry;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.omnisheetvault.api.ruleset.SheetCalculator;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SheetCalculatorRegistry {

    private final Map<String, SheetCalculator> bySystem;

    public SheetCalculatorRegistry(List<SheetCalculator> calculators) {
        this.bySystem = calculators.stream().collect(toMap(SheetCalculator::systemId, identity()));
    }

    public SheetCalculator forSystem(String systemId) {
        SheetCalculator calculator = bySystem.get(systemId);
        if (calculator == null) {
            throw new UnsupportedGameSystemException(systemId);
        }
        return calculator;
    }
}
