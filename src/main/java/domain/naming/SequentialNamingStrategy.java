package domain.naming;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class SequentialNamingStrategy implements NameGenerationStrategy {
    @Override
    public String generateName(String prefix, Collection<?> items) {
        Set<String> existingNames = items.stream()
            .map(item -> {
                try {
                    return (String) item.getClass().getMethod("getName").invoke(item);
                } catch (Exception e) {
                    throw new RuntimeException("All items must have getName()", e);
                }
            })
            .collect(Collectors.toSet());

        int i = 1;
        while (existingNames.contains(prefix + i)) i++;
        return prefix + i;
    }
}
