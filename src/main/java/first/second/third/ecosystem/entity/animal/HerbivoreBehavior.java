package first.second.third.ecosystem.entity.animal;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;

import java.util.HashMap;

public class HerbivoreBehavior extends CreatureBehavior {

    @Override
    protected HashMap<String, CreatureStatistics> getConsumers() {
        return processor.getHerbivores(); // Возвращаем травоядных как потребителей
    }

    @Override
    protected HashMap<String, CreatureStatistics> getConsumed() {
        return processor.getPlants(); // Возвращаем растения как тех, кого едят
    }

    @Override
    protected String getConsumersCategory() {
        return "HERBIVORE";
    }

    @Override
    protected String getConsumedCategory() {
        return "PLANT";
    }
}