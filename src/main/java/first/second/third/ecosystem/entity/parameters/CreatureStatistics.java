package first.second.third.ecosystem.entity.parameters;

import lombok.Getter;
import lombok.Setter;

// Класс, представляющий статистику существа в экосистеме
@Setter
@Getter
public class CreatureStatistics {
    // Количество существ данного вида
    private long count;
    // Комфортная температура для существ
    private int comfortTemperature;
    // Количество воды, потребляемой одним существом
    private int consumedWaterOnSingleCreature;
    // Комфортный уровень влажности для существ
    private int comfortHumidityPercentage;

    // Конструктор класса CreatureStatistics
    public CreatureStatistics(long count, int comfortTemperature, int consumedWaterOnSingleCreature, int comfortHumidityPercentage) {
        this.count = count;
        this.comfortTemperature = comfortTemperature;
        this.consumedWaterOnSingleCreature = consumedWaterOnSingleCreature;
        this.comfortHumidityPercentage = comfortHumidityPercentage;
    }

    // Метод для расчета фактора вымирания на основе условий окружающей среды
    public double calculateExtinctionFactor(WorldConditions worldConditions) {
        double temperatureFactor = calculateTemperatureFactor(worldConditions.getTemperature(), comfortTemperature);
        double humidityFactor = calculateHumidityFactor(worldConditions.getHumidityPercentage(), comfortHumidityPercentage);
        double waterFactor = calculateWaterFactor(worldConditions.getAccessibleWater(), consumedWaterOnSingleCreature);
        // Возвращаем общий фактор вымирания
        return 1 / (temperatureFactor * humidityFactor * waterFactor);
    }

    // Метод для расчета фактора рождаемости на основе условий окружающей среды
    public double calculateBirthFactor(WorldConditions worldConditions) {
        double temperatureFactor = calculateTemperatureFactor(worldConditions.getTemperature(), comfortTemperature);
        double humidityFactor = calculateHumidityFactor(worldConditions.getHumidityPercentage(), comfortHumidityPercentage);
        double waterFactor = calculateWaterFactor(worldConditions.getAccessibleWater(), consumedWaterOnSingleCreature);

        // Итоговый коэффициент рождаемости (чем лучше условия, тем выше результат)
        return temperatureFactor * humidityFactor * waterFactor / 1.5;
    }

    // Метод для расчета коэффициента температуры
    private double calculateTemperatureFactor(int currentTemp, int comfortTemp) {
        double delta = Math.abs(currentTemp - comfortTemp); // Разница между текущей и комфортной температурой
        if (delta == 0) return 1.2; // Идеальные условия
        if (delta <= 5) return 1.1; // Небольшие отклонения
        if (delta <= 10) return 1.0; // Умеренные отклонения
        if (delta <= 20) return 0.8; // Значительные отклонения
        return 0.5; // Критические условия
    }

    // Метод для расчета коэффициента влажности
    private double calculateHumidityFactor(int currentHumidity, int comfortHumidity) {
        double delta = Math.abs(currentHumidity - comfortHumidity); // Разница между текущей и комфортной влажностью
        if (delta == 0) return 1.2; // Идеальные условия
        if (delta <= 5) return 1.1; // Небольшие отклонения
        if (delta <= 20) return 1.0; // Умеренные отклонения
        if (delta <= 50) return 0.8; // Значительные отклонения
        return 0.5; // Критические условия
    }

    // Метод для расчета коэффициента воды
    private double calculateWaterFactor(int accessibleWater, int consumedWater) {
        int deltaWater = accessibleWater - consumedWater; // Разница между доступной водой и потребляемой
        if (deltaWater >= 0) return 1.4; // Достаточно воды
        if (deltaWater >= -50) return 0.9; // Небольшой дефицит воды
        if (deltaWater >= -100) return 0.8; // Умеренный дефицит воды
        if (deltaWater >= -150) return 0.6; // Значительный дефицит воды
        return 0.3; // Критический дефицит воды
    }
}
