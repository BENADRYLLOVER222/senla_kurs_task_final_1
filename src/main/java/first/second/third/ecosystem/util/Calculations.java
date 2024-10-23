package first.second.third.ecosystem.util;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;
import first.second.third.ecosystem.entity.parameters.WorldConditions;

import java.util.Random;

import static first.second.third.ecosystem.util.Constants.POPULATION_CALCULATIONS_BORDER;
import static first.second.third.ecosystem.util.Constants.TAKE_PERCENT;

public class Calculations {

    public int calculateDeaths(double livingCreatures, double extinctionFactor) {
        Random random = new Random();
        if(livingCreatures > POPULATION_CALCULATIONS_BORDER){
            return (int) Math.ceil(livingCreatures * TAKE_PERCENT * extinctionFactor * random.nextDouble(90, 200) / 100);
        }
        return (int) Math.ceil(livingCreatures * TAKE_PERCENT * extinctionFactor * Math.round(random.nextDouble(90, 200)/ 100));
    }

    public int calculateBirth(double livingCreatures, double birthFactor) {
        Random random = new Random();

        if (livingCreatures > POPULATION_CALCULATIONS_BORDER) {
            // Для больших популяций: слегка увеличиваем фактор случайности
            return (int) Math.ceil(livingCreatures * TAKE_PERCENT * birthFactor * random.nextDouble(0, 1.2));
        }
        return (int) Math.ceil(livingCreatures * TAKE_PERCENT * birthFactor * random.nextDouble(0, 1.2));
    }

    // Метод для оценки шансов выживания травоядных
    public static double calculateHerbivoreSurvivalChance(CreatureStatistics herbivoreStats, long totalPredators, long totalPlants, WorldConditions worldConditions) {
        double predatorHerbivoreRatio = totalPredators > 0 ? (double) herbivoreStats.getCount() / totalPredators : Double.MAX_VALUE;
        double plantHerbivoreRatio = totalPlants > 0 ? (double) totalPlants / herbivoreStats.getCount() : 0;

        double extinctionFactor = herbivoreStats.calculateExtinctionFactor(worldConditions);
        double birthFactor = herbivoreStats.calculateBirthFactor(worldConditions);

        double survivalChance = (birthFactor - extinctionFactor) * predatorHerbivoreRatio * plantHerbivoreRatio;

        return Math.max(survivalChance, 0);
    }

    // Метод для оценки шансов выживания хищников
    public static double calculatePredatorSurvivalChance(CreatureStatistics predatorStats, long totalHerbivores, long totalDecomposers, WorldConditions worldConditions) {
        double herbivorePredatorRatio = totalHerbivores > 0 ? (double) totalHerbivores / predatorStats.getCount() : 0;
        double decomposerEffect = totalDecomposers > 0 ? (double) totalDecomposers / predatorStats.getCount() : 0.5;

        double extinctionFactor = predatorStats.calculateExtinctionFactor(worldConditions);
        double birthFactor = predatorStats.calculateBirthFactor(worldConditions);

        double survivalChance = (birthFactor - extinctionFactor) * herbivorePredatorRatio / (1 + decomposerEffect);

        return Math.max(survivalChance, 0);
    }

    // Метод для оценки шансов выживания растений
    public static double calculatePlantSurvivalChance(CreatureStatistics plantStats, long totalHerbivores, WorldConditions worldConditions) {
        double herbivorePlantRatio = totalHerbivores > 0 ? (double) totalHerbivores / plantStats.getCount() : 0;

        double extinctionFactor = plantStats.calculateExtinctionFactor(worldConditions);
        double birthFactor = plantStats.calculateBirthFactor(worldConditions);

        double survivalChance = (birthFactor - extinctionFactor) / (1 + herbivorePlantRatio);

        return Math.max(survivalChance, 0);
    }

    // Метод для оценки шансов выживания декомпозиторов
    public static double calculateDecomposerSurvivalChance(CreatureStatistics decomposerStats, long totalPredators, WorldConditions worldConditions) {
        double predatorDecomposerRatio = totalPredators > 0 ? (double) totalPredators / decomposerStats.getCount() : 0;

        double extinctionFactor = decomposerStats.calculateExtinctionFactor(worldConditions);
        double birthFactor = decomposerStats.calculateBirthFactor(worldConditions);

        double survivalChance = (birthFactor - extinctionFactor) * predatorDecomposerRatio;

        return Math.max(survivalChance, 0);
    }
}
