package first.second.third.ecosystem.util;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;
import first.second.third.ecosystem.entity.parameters.WorldConditions;
import first.second.third.ecosystem.entity.animal.CreatureBehavior;
import first.second.third.ecosystem.entity.animal.DecomposerBehavior;
import first.second.third.ecosystem.entity.animal.HerbivoreBehavior;
import first.second.third.ecosystem.entity.animal.PlantBehavior;
import first.second.third.ecosystem.entity.animal.PredatorBehavior;
import first.second.third.ecosystem.exception.UnknownCategoryException;
import first.second.third.ecosystem.fileprocessor.EcoSystemFileProcessor;

import java.util.ConcurrentModificationException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static first.second.third.ecosystem.util.Calculations.*;

public class SimulationConsole {

    // Logger для записи логов
    private static final Logger logger = LoggerFactory.getLogger(SimulationConsole.class);
    private static final Scanner scanner = new Scanner(System.in);
    private final EcoSystemFileProcessor processor; // Процессор для работы с файлами экосистемы
    private final WorldConditions worldConditions; // Условия мира

    private boolean isRunning; // Флаг для проверки состояния симуляции

    public SimulationConsole() {
        processor = EcoSystemFileProcessor.getInstance();
        worldConditions = WorldConditions.getInstance();
        isRunning = false; // Изначально симуляция не запущена
    }

    // Запуск симуляции
    public void start() {
        logger.info("Simulation Console started.");
        String input;

        while (true) {
            // Вывод доступных команд
            System.out.println("\nAvailable commands: start, stop, add, change, display, save, load, exit");
            System.out.print("Enter command: ");
            input = scanner.nextLine().trim().toLowerCase();

            // Обработка введенной команды
            switch (input) {
                case "start":
                    startSimulation();
                    break;
                case "stop":
                    stopSimulation();
                    break;
                case "add":
                    addCreature();
                    break;
                case "change":
                    changeConditions();
                    break;
                case "display":
                    displayPopulation();
                    break;
                case "save":
                    saveToFile(worldConditions);
                    break;
                case "load":
                    loadFromFile();
                    break;
                case "exit":
                    stopSimulation();
                    logger.info("Exiting the simulation.");
                    return; // Выход из метода
                default:
                    System.out.println("Unknown command: " + input); // Неверная команда
                    logger.warn("Unknown command: {}", input);
            }
        }
    }

    // Метод для начала симуляции
    private void startSimulation() {
        if (isRunning) {
            System.out.println("Simulation is already running."); // Проверка на повторный запуск
        } else {
            isRunning = true; // Устанавливаем флаг
            new Thread(this::runSimulation).start(); // Запускаем симуляцию в отдельном потоке
            System.out.println("Simulation started.");
        }
    }

    // Метод для остановки симуляции
    private void stopSimulation() {
        if (isRunning) {
            isRunning = false; // Устанавливаем флаг остановки
            System.out.println("Simulation stopped.");
            logger.info("Simulation stopped.");
        } else {
            System.out.println("Simulation is not running."); // Если симуляция не запущена
            logger.warn("Simulation is not running.");
        }
    }

    // Основной метод симуляции
    private void runSimulation() {
        PredatorBehavior predator = new PredatorBehavior(); // Создаем поведение хищника
        HerbivoreBehavior herbivore = new HerbivoreBehavior(); // Создаем поведение травоядного
        PlantBehavior plant = new PlantBehavior(); // Создаем поведение растения
        DecomposerBehavior decomposer = new DecomposerBehavior(); // Создаем поведение разлагателя
        try {
            while (isRunning) {
                try {
                    // Вывод текущего года симуляции
                    System.out.println("--------------------YEARS : " + worldConditions.getYear() + " --------------------------------");
                    System.out.println("-----------------------------------------------------------------------");

                    // Выполнение действий для всех существ в экосистеме
                    processor.getPredators().forEach((species, stats) -> animalActions(species, predator, stats, worldConditions));
                    processor.getHerbivores().forEach((species, stats) -> animalActions(species, herbivore, stats, worldConditions));
                    processor.getPlants().forEach((species, stats) -> animalActions(species, plant, stats, worldConditions));
                    processor.getDecomposers().forEach((species, stats) -> animalActions(species, decomposer, stats, worldConditions));
                    worldConditions.setYear(worldConditions.getYear() + 1); // Увеличение года
                    logger.info("Simulation year progressed to: {}", worldConditions.getYear());
                    Thread.sleep(50); // Задержка между циклами
                } catch (ConcurrentModificationException | InterruptedException e) {
                    if (!isRunning) break; // Если симуляция остановлена, выходим из цикла
                    logger.warn("Exception during simulation cycle: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error during simulation: " + e.getMessage()); // Обработка ошибок
            logger.error("Critical error during simulation: {}", e.getMessage());
        } finally {
            System.out.println("Simulation fully stopped.");
            logger.info("Simulation fully stopped.");
        }
    }

    // Метод для добавления существа
    private void addCreature() {
        try {
            // Запрос информации о новом существе
            System.out.print("Enter category (HERBIVORE, PREDATOR, PLANT, DECOMPOSER): ");
            String category = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter species name: ");
            String species = scanner.nextLine().trim().toUpperCase();
            System.out.print("Enter population count: ");
            long count = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter comfort temperature: ");
            int comfortTemperature = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter consumed water on population: ");
            int consumedWaterOnPopulation = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter comfort humidity percentage: ");
            int comfortHumidityPercentage = Integer.parseInt(scanner.nextLine().trim());
            try {
                // Добавление существа через процессор
                processor.addCreature(category, species, count, comfortTemperature, consumedWaterOnPopulation, comfortHumidityPercentage);
                System.out.println("Creature added successfully.");
                logger.info("Creature added: {}, species: {}, count: {}", category, species, count);
            } catch (UnknownCategoryException e) {
                System.out.println(e.getMessage()); // Обработка неизвестной категории
                logger.error(e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter valid numbers."); // Обработка неверного формата ввода
            logger.error("Invalid number format during creature addition: {}", e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred while adding the creature: " + e.getMessage()); // Обработка других ошибок
            logger.error("Error adding creature: {}", e.getMessage());
        }
    }

    // Метод для изменения условий мира
    private void changeConditions() {
        try {
            // Запрос новых условий
            System.out.print("Enter new temperature: ");
            int newTemp = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter accessible water: ");
            int newWater = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter comfort humidity percentage: ");
            int newHumidity = Integer.parseInt(scanner.nextLine().trim());

            // Установка новых условий
            worldConditions.setTemperature(newTemp);
            worldConditions.setAccessibleWater(newWater);
            worldConditions.setHumidityPercentage(newHumidity);

            System.out.println("World conditions updated.");
            logger.info("World conditions updated: temperature={}, water={}, humidity={}", newTemp, newWater, newHumidity);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter valid numbers."); // Обработка неверного ввода
            logger.error("Invalid input for world conditions: {}", e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred while changing conditions: " + e.getMessage()); // Обработка других ошибок
            logger.error("Error changing world conditions: {}", e.getMessage());
        }
    }

    private void displayPopulation() {
        // Вывод текущих условий и популяции
        System.out.println("Current year: " + worldConditions.getYear());
        System.out.println("Current temperature: " + worldConditions.getTemperature());
        System.out.println("Current accessible water: " + worldConditions.getAccessibleWater());
        System.out.println("Current comfort humidity percentage: " + worldConditions.getHumidityPercentage());
        System.out.println("Current ecosystem populations:");

        // Подсчитываем общее количество хищников, травоядных, растений и декомпозиторов
        long totalPredators = processor.getPredators().values().stream()
                .mapToLong(CreatureStatistics::getCount)
                .sum();
        long totalHerbivores = processor.getHerbivores().values().stream()
                .mapToLong(CreatureStatistics::getCount)
                .sum();
        long totalPlants = processor.getPlants().values().stream()
                .mapToLong(CreatureStatistics::getCount)
                .sum();
        long totalDecomposers = processor.getDecomposers().values().stream()
                .mapToLong(CreatureStatistics::getCount)
                .sum();

        // Выводим численности
        System.out.println("Total number of predators: " + totalPredators);
        System.out.println("Total number of herbivores: " + totalHerbivores);
        System.out.println("Total number of plants: " + totalPlants);
        System.out.println("Total number of decomposers: " + totalDecomposers);

        // Вывод информации о каждом виде
        processor.getPredators().forEach((species, stats) -> {
            System.out.print("PREDATOR: " + species + " -> " + stats.getCount());

            // Рассчитываем шансы на выживание для хищников
            double survivalChance = calculatePredatorSurvivalChance(stats, totalHerbivores, totalDecomposers, worldConditions);
            System.out.println("; survival coefficient: " + survivalChance);
        });

        processor.getHerbivores().forEach((species, stats) -> {
            System.out.print("HERBIVORE: " + species + " -> " + stats.getCount());

            // Рассчитываем шансы на выживание для травоядных
            double survivalChance = calculateHerbivoreSurvivalChance(stats, totalPredators, totalPlants, worldConditions);
            System.out.println("; survival coefficient: " + survivalChance);
        });

        processor.getPlants().forEach((species, stats) -> {
            System.out.print("PLANT: " + species + " -> " + stats.getCount());

            // Рассчитываем шансы на выживание для растений
            double survivalChance = calculatePlantSurvivalChance(stats, totalHerbivores, worldConditions);
            System.out.println("; survival coefficient: " + survivalChance);
        });

        processor.getDecomposers().forEach((species, stats) -> {
            System.out.print("DECOMPOSER: " + species + " -> " + stats.getCount());

            // Рассчитываем шансы на выживание для декомпозиторов
            double survivalChance = calculateDecomposerSurvivalChance(stats, totalPredators, worldConditions);
            System.out.println("; survival coefficient: " + survivalChance);
        });
    }


    // Метод для сохранения состояния мира в файл
    private void saveToFile(WorldConditions worldConditions) {
        processor.saveToFile(worldConditions, SimulationConsole.scanner);
    }

    // Метод для загрузки состояния мира из файла
    private void loadFromFile() {
        System.out.print("Enter file path to load: ");
        String filePath = scanner.nextLine().trim();
        processor.loadFromFile(filePath); // Загрузка из файла
    }

    // Статический метод для выполнения действий существ
    static void animalActions(String species, CreatureBehavior creature, CreatureStatistics stats, WorldConditions worldConditions) {
        creature.die(species, worldConditions); // Умирание существа
        creature.reproduce(species, worldConditions); // Размножение существа
        creature.eatRandom(species); // Питание существа
        System.out.println("ANIMAL: " + species + " population: " + stats.getCount());
        System.out.println("---------------------------------------------------------------");
    }
}
