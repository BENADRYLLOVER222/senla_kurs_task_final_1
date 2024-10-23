package first.second.third.ecosystem.fileprocessor;


import first.second.third.ecosystem.entity.parameters.WorldConditions;
import first.second.third.ecosystem.entity.parameters.CreatureStatistics;
import first.second.third.ecosystem.exception.UnknownCategoryException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

@Getter
@NoArgsConstructor
public class EcoSystemFileProcessor {
    private final HashMap<String, CreatureStatistics> plants = new HashMap<>();
    private final HashMap<String, CreatureStatistics> herbivores = new HashMap<>();
    private final HashMap<String, CreatureStatistics> predators = new HashMap<>();
    private final HashMap<String, CreatureStatistics> decomposers = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(EcoSystemFileProcessor.class);  // Получаем логгер

    private static EcoSystemFileProcessor instance;


    public static EcoSystemFileProcessor getInstance() {
        if (instance == null) {
            instance = new EcoSystemFileProcessor();
        }
        return instance;
    }

    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            WorldConditions worldConditions = WorldConditions.getInstance(); // Получаем экземпляр WorldConditions

            int lineNumber = 0;  // Счётчик строк
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Убираем пробелы

                if (line.startsWith("//")) {
                    continue; // Пропускаем строки с комментариями
                }

                switch (lineNumber) {
                    case 0:
                        worldConditions.setYear(Integer.parseInt(line));
                        System.out.println("Loaded simulation year: " + line);  // Выводим пользователю
                        break;
                    case 1:
                        worldConditions.setTemperature(Integer.parseInt(line));
                        System.out.println("Loaded temperature: " + line);  // Выводим пользователю
                        break;
                    case 2:
                        worldConditions.setAccessibleWater(Integer.parseInt(line));
                        System.out.println("Loaded water: " + line);  // Выводим пользователю
                        break;
                    case 3:
                        worldConditions.setHumidityPercentage(Integer.parseInt(line));
                        System.out.println("Loaded humidity: " + line);  // Выводим пользователю
                        break;
                    default:
                        // Обработка существ
                        String[] parts = line.split(",");
                        if (parts.length < 6) {
                            logger.warn("Invalid line format: {}. Expected at least 6 parts.", line);
                            continue; // Пропускаем строки с недостаточным количеством элементов
                        }
                        String category = parts[0].trim().toUpperCase();
                        String type = parts[1].trim().toUpperCase();
                        long count = Integer.parseInt(parts[2].trim());
                        int comfortTemperature = Integer.parseInt(parts[3].trim());
                        int consumedWaterOnPopulation = Integer.parseInt(parts[4].trim());
                        int comfortHumidityPercentage = Integer.parseInt(parts[5].trim());

                        CreatureStatistics stats = new CreatureStatistics(count, comfortTemperature, consumedWaterOnPopulation, comfortHumidityPercentage);

                        switch (category) {
                            case "HERBIVORE":
                                herbivores.put(type, stats);
                                break;
                            case "PREDATOR":
                                predators.put(type, stats);
                                break;
                            case "PLANT":
                                plants.put(type, stats);
                                break;
                            case "DECOMPOSER":
                                decomposers.put(type, stats);
                                break;
                            default:
                                logger.warn("Unknown category: {}", category);  // Логгируем как предупреждение
                        }
                        break;
                }
                lineNumber++;
            }
            System.out.println("Ecosystem loaded successfully from " + filename);  // Сообщение пользователю
            logger.info("File {} loaded successfully.", filename);  // Логгируем успешную загрузку

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());  // Сообщение пользователю
            logger.error("Error reading file {}: {}", filename, e.getMessage());  // Логгируем ошибку
        } catch (IllegalArgumentException e) {
            System.out.println("Error processing data from file: " + e.getMessage());  // Сообщение пользователю
            logger.error("Error processing data from file {}: {}", filename, e.getMessage());  // Логгируем ошибку
        }
    }


    public void addCreature(String category, String species, long count, int comfortTemperature, int consumedWaterOnPopulation, int comfortHumidityPercentage) {
        CreatureStatistics stats = new CreatureStatistics(count, comfortTemperature, consumedWaterOnPopulation, comfortHumidityPercentage);
        switch (category) {
            case "HERBIVORE":
                herbivores.put(species, stats);
                break;
            case "PREDATOR":
                predators.put(species, stats);
                break;
            case "PLANT":
                plants.put(species, stats);
                break;
            case "DECOMPOSER":
                decomposers.put(species, stats);
                break;
            default:
                throw new UnknownCategoryException(category);
        }
    }

    public void killCreature(String category, String species, long count) {
        CreatureStatistics stats;
        switch (category) {
            case "HERBIVORE":
                stats = herbivores.get(species);
                updateCount(stats, species, herbivores, -count);
                break;
            case "PREDATOR":
                stats = predators.get(species);
                updateCount(stats, species, predators, -count);
                break;
            case "PLANT":
                stats = plants.get(species);
                updateCount(stats, species, plants, -count);
                break;
            case "DECOMPOSER":
                stats = decomposers.get(species);
                updateCount(stats, species, decomposers, -count);
                break;
            default:
                throw new UnknownCategoryException(category);
        }
    }

    public void createCreature(String category, String species, long count) {
        CreatureStatistics stats;
        switch (category) {
            case "HERBIVORE":
                stats = herbivores.get(species);
                updateCount(stats, species, herbivores, count);
                break;
            case "PREDATOR":
                stats = predators.get(species);
                updateCount(stats, species, predators, count);
                break;
            case "PLANT":
                stats = plants.get(species);
                updateCount(stats, species, plants, count);
                break;
            case "DECOMPOSER":
                stats = decomposers.get(species);
                updateCount(stats, species, decomposers, count);
                break;
            default:
                System.out.println("Unknown category: " + category);
        }
    }

    public void updateCount(CreatureStatistics stats, String species, HashMap<String, CreatureStatistics> map, long count) {
        Optional.ofNullable(stats).ifPresentOrElse(existingStats -> {
            if (existingStats.getCount() <= 0) {
                map.remove(species);
            } else {
                existingStats.setCount(existingStats.getCount() + count);
            }
        }, () -> System.out.println("No such creature found"));
    }

    public void updateCreature(String category, String species, long count) {
        try {
            switch (category.toUpperCase()) {
                case "HERBIVORE":
                    herbivores.merge(species, new CreatureStatistics(count, getComfortTemperature(species, herbivores), 0, 0),
                            (existingStats, newStat) -> {
                                existingStats.setCount(existingStats.getCount() + newStat.getCount());
                                return existingStats;
                            });
                    break;
                case "PREDATOR":
                    predators.merge(species, new CreatureStatistics(count, getComfortTemperature(species, predators), 0, 0),
                            (existingStats, newStat) -> {
                                existingStats.setCount(existingStats.getCount() + newStat.getCount());
                                return existingStats;
                            });
                    break;
                case "PLANT":
                    plants.merge(species, new CreatureStatistics(count, getComfortTemperature(species, plants), 0, 0),
                            (existingStats, newStat) -> {
                                existingStats.setCount(existingStats.getCount() + newStat.getCount());
                                return existingStats;
                            });
                    break;
                case "DECOMPOSER":
                    decomposers.merge(species, new CreatureStatistics(count, getComfortTemperature(species, decomposers), 0, 0),
                            (existingStats, newStat) -> {
                                existingStats.setCount(existingStats.getCount() + newStat.getCount());
                                return existingStats;
                            });
                    break;
                default:
                    System.out.println("Unknown category: " + category);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error processing data: " + e.getMessage());
        }
    }

    public void saveToFile(WorldConditions worldConditions, Scanner scanner) {
        System.out.print("Enter file path to save: ");
        String filePath = scanner.nextLine().trim(); // Получаем путь к файлу от пользователя

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            // Сохраняем условия мира
            System.out.println("Saving ecosystem to " + filePath);  // Сообщение пользователю
            logger.info("Saving ecosystem to " + filePath);  // Логгируем сохранение

            writer.write(String.valueOf(worldConditions.getYear()));
            writer.newLine();
            writer.write(String.valueOf(worldConditions.getTemperature()));
            writer.newLine();
            writer.write(String.valueOf(worldConditions.getAccessibleWater()));
            writer.newLine();
            writer.write(String.valueOf(worldConditions.getHumidityPercentage()));
            writer.newLine();

            // Сохраняем статистику существ
            saveCreatures(writer, "PLANT", plants);
            saveCreatures(writer, "HERBIVORE", herbivores);
            saveCreatures(writer, "PREDATOR", predators);
            saveCreatures(writer, "DECOMPOSER", decomposers);

            System.out.println("Ecosystem saved successfully to " + filePath);  // Сообщение пользователю
            logger.info("Ecosystem saved successfully to {}", filePath);  // Логгируем успешное сохранение
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());  // Сообщение пользователю об ошибке
            logger.error("Error saving to file: {}", e.getMessage());  // Логгируем ошибку
        }
    }

    private void saveCreatures(BufferedWriter writer, String category, HashMap<String, CreatureStatistics> creatures) throws IOException {
        for (var entry : creatures.entrySet()) {
            String type = entry.getKey();
            CreatureStatistics stats = entry.getValue();

            // Записываем данные о каждом существе в формате: CATEGORY, TYPE, COUNT, COMFORT_TEMPERATURE, CONSUMED_WATER, COMFORT_HUMIDITY
            writer.write(String.format("%s,%s,%d,%d,%d,%d", category, type, stats.getCount(), stats.getComfortTemperature(), stats.getConsumedWaterOnSingleCreature(), stats.getComfortHumidityPercentage()));
            writer.newLine(); // Переход на новую строку
        }
    }
    // Метод для получения комфортной температуры существ из карты
    private int getComfortTemperature(String species, HashMap<String, CreatureStatistics> map) {
        CreatureStatistics stats = map.get(species);
        return stats != null ? stats.getComfortTemperature() : 0; // Возвращаем 0, если не найдено
    }
}

