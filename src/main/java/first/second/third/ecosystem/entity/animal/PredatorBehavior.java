package first.second.third.ecosystem.entity.animal;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;

import java.util.HashMap;

public class PredatorBehavior extends CreatureBehavior {
    @Override
    protected HashMap<String, CreatureStatistics> getConsumers() {
        return processor.getPredators(); // Возвращаем растения как потребителей
    }

    @Override
    protected HashMap<String, CreatureStatistics> getConsumed() {
        return processor.getHerbivores(); // Возвращаем разложители как тех, кого едят
    }

    @Override
    protected String getConsumersCategory() {
        return "PREDATOR";
    }

    @Override
    protected String getConsumedCategory() {
        return "HERBIVORE";
    }
}
