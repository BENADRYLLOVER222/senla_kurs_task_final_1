package first.second.third.ecosystem.entity.animal;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;

import java.util.HashMap;

public class DecomposerBehavior extends CreatureBehavior {
    @Override
    protected HashMap<String, CreatureStatistics> getConsumers() {
        return processor.getDecomposers(); // Возвращаем растения как потребителей
    }

    @Override
    protected HashMap<String, CreatureStatistics> getConsumed() {
        return processor.getPredators(); // Возвращаем разложители как тех, кого едят
    }

    @Override
    protected String getConsumersCategory() {
        return "DECOMPOSER";
    }

    @Override
    protected String getConsumedCategory() {
        return "PREDATOR";
    }
}
