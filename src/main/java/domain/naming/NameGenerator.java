package domain.naming;

import java.util.Collection;

public class NameGenerator {
    private final NameGenerationStrategy strategy;
    
    public NameGenerator(NameGenerationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public String generateUniqueName(String prefix, Collection<?> items) {
        return strategy.generateName(prefix, items);
    }
}
