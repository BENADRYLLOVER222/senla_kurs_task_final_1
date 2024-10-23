package first.second.third.ecosystem.entity.animal;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;

import java.util.HashMap;

public class PlantBehavior extends CreatureBehavior {

    @Override
    protected HashMap<String, CreatureStatistics> getConsumers() {
        return processor.getPlants(); // Возвращаем растения как потребителей
    }

    @Override
    protected HashMap<String, CreatureStatistics> getConsumed() {
        return processor.getDecomposers(); // Возвращаем разложители как тех, кого едят
    }

    @Override
    protected String getConsumersCategory() {
        return "PLANT";
    }

    @Override
    protected String getConsumedCategory() {
        return "DECOMPOSER";
    }
}
