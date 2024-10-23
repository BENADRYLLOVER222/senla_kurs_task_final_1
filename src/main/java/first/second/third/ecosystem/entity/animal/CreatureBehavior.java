package first.second.third.ecosystem.entity.animal;

import first.second.third.ecosystem.entity.parameters.CreatureStatistics;
import first.second.third.ecosystem.entity.parameters.WorldConditions;
import first.second.third.ecosystem.fileprocessor.EcoSystemFileProcessor;
import first.second.third.ecosystem.util.Calculations;
import first.second.third.ecosystem.exception.UnknownCategoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static first.second.third.ecosystem.util.Constants.HUNGER_PERCENT;
import static first.second.third.ecosystem.util.Constants.TAKE_PERCENT;

public abstract class CreatureBehavior {

    // Получаем экземпляр класса для работы с файлами экосистемы
    protected final EcoSystemFileProcessor processor = EcoSystemFileProcessor.getInstance();
    private final Calculations calculations = new Calculations();
    private static final Logger logger = LoggerFactory.getLogger(CreatureBehavior.class);

    // Абстрактные методы для получения информации о потребителях и съеденных существах
    protected abstract HashMap<String, CreatureStatistics> getConsumers();
    protected abstract HashMap<String, CreatureStatistics> getConsumed();
    protected abstract String getConsumersCategory();
    protected abstract String getConsumedCategory();

    /**
     * Метод для обработки процесса поедания конкретного существа.
     *
     * @param consumed Название съеденного существа.
     * @param consumer Название существа-потребителя.
     * @param count Количество, которое нужно съесть.
     */
    public void eat(String consumed, String consumer, long count) {
        HashMap<String, CreatureStatistics> consumers = getConsumers();
        HashMap<String, CreatureStatistics> consumedMap = getConsumed();

        // Проверяем, существует ли потребитель и съедаемое существо, и есть ли у него запас
        if (consumers.containsKey(consumer.toUpperCase()) &&
                consumedMap.containsKey(consumed.toUpperCase()) &&
                consumedMap.get(consumed.toUpperCase()).getCount() > 0) {

            // Убиваем указанное количество съеденного существа
            processor.killCreature(getConsumedCategory(), consumed.toUpperCase(), count);
            String message = consumer + " is eating " + consumed;
            logger.info(message);
            System.out.println(message); // Вывод сообщения для пользователя
        } else {
            // Если условия не выполнены, выводим предупреждение
            String message = "There is no such " + consumed + " or " + consumer + " in the ecosystem";
            logger.warn(message);
            System.out.println(message); // Вывод сообщения для пользователя
        }
    }

    /**
     * Метод для обработки случайного поедания существ.
     *
     * @param consumer Название существа-потребителя.
     */
    public void eatRandom(String consumer) {

        HashMap<String, CreatureStatistics> consumers = getConsumers();
        HashMap<String, CreatureStatistics> consumedMap = getConsumed();

        // Проверяем, существует ли потребитель
        if (!consumers.containsKey(consumer.toUpperCase())) {
            String message = "There is no such " + consumer + " in the ecosystem";
            logger.warn(message);
            System.out.println(message); // Вывод сообщения для пользователя
            return; // Выход из метода, если потребитель не найден
        }

        double population = consumers.get(consumer.toUpperCase()).getCount();
        // Список существ, которые могут быть съедены
        List<String> consumableCreatures = consumedMap.entrySet().stream()
                .filter(entry -> entry.getValue().getCount() > 0)
                .map(Map.Entry::getKey)
                .toList();

        // Проверяем, есть ли доступная пища
        try {
            if (!consumableCreatures.isEmpty()) {
                Random random = new Random();
                // Выбираем случайное существо для поедания
                String randomConsumed = consumableCreatures.get(random.nextInt(consumableCreatures.size()));

                // Рассчитываем количество, которое может быть съедено
                long eatableCount = (long) Math.ceil(population * HUNGER_PERCENT);
                long actualEaten = Math.min(eatableCount, consumedMap.get(randomConsumed).getCount());

                // Рассчитываем количество существ, которые могут умереть от голода
                long hungerDeath = eatableCount > actualEaten ? (eatableCount - actualEaten) : 0;
                String message = consumer + "S are eating " + actualEaten + " " + randomConsumed + "S";
                logger.info(message);
                System.out.println(message); // Вывод сообщения для пользователя

                // Если есть существа, которые могут умереть от голода, убиваем их
                if (hungerDeath > 0) {
                    processor.killCreature(getConsumersCategory(), consumer.toUpperCase(), hungerDeath);
                    String deathMessage = hungerDeath + " " + consumer + "S have died of hunger";
                    logger.warn(deathMessage);
                    System.out.println(deathMessage); // Вывод сообщения для пользователя
                }
            } else {
                // Если пищи нет, рассчитываем количество существ, которые могут умереть от голода
                double hungerDeath = Math.ceil(population / 10);
                processor.killCreature(getConsumersCategory(), consumer.toUpperCase(), (long) hungerDeath);
                String message = "No available food for " + consumer + " in the ecosystem";
                logger.warn(message);
                System.out.println(message); // Вывод сообщения для пользователя
                message = hungerDeath + " " + consumer + "S have died of hunger";
                logger.warn(message);
                System.out.println(message); // Вывод сообщения для пользователя
            }
        } catch (UnknownCategoryException e) {
            // Обрабатываем исключение, если возникла ошибка с категорией
            String errorMessage = "Error while processing " + consumer + ": " + e.getMessage();
            logger.error(errorMessage);
            System.out.println(errorMessage); // Выводим сообщение об ошибке для пользователя
        } catch (Exception e) {
            // Обрабатываем любые другие исключения
            String errorMessage = "An unexpected error occurred while processing " + consumer + ": " + e.getMessage();
            logger.error(errorMessage);
            System.out.println(errorMessage); // Выводим сообщение об ошибке для пользователя
        }
    }

    /**
     * Метод для обработки смерти существа.
     *
     * @param consumer Название существа, которое умирает.
     * @param worldConditions Условия мира, влияющие на смерть.
     */
    public void die(String consumer, WorldConditions worldConditions) {
        HashMap<String, CreatureStatistics> consumers = getConsumers();
        double population = consumers.get(consumer.toUpperCase()).getCount();
        // Рассчитываем количество смертей на основе условий
        long deathCount = calculations.calculateDeaths(population, consumers.get(consumer.toUpperCase()).calculateExtinctionFactor(worldConditions));

        // Проверяем, существует ли потребитель
        if (consumers.containsKey(consumer.toUpperCase())) {
            // Убиваем количество существ, которое рассчитано
            processor.killCreature(getConsumersCategory(), consumer.toUpperCase(), deathCount);
            String message = deathCount + " " + consumer + "S have died";
            logger.info(message);
            System.out.println(message); // Вывод сообщения для пользователя
        } else {
            // Если потребитель не найден, выводим предупреждение
            String message = "There is no such " + consumer + " in the ecosystem";
            logger.warn(message);
            System.out.println(message); // Вывод сообщения для пользователя
        }
    }

    /**
     * Метод для обработки размножения существа.
     *
     * @param consumer Название существа, которое размножается.
     * @param worldConditions Условия мира, влияющие на размножение.
     */
    public void reproduce(String consumer, WorldConditions worldConditions) {
        HashMap<String, CreatureStatistics> consumers = getConsumers();
        CreatureStatistics stats = consumers.get(consumer.toUpperCase());

        // Проверяем, существует ли существо и достаточно ли его популяции для размножения
        if (stats != null && stats.getCount() >= 1) {
            long population = stats.getCount();
            // Рассчитываем количество новых существ, которые могут родиться
            long birthCount = calculations.calculateBirth(population, stats.calculateBirthFactor(worldConditions));

            int luck = 1; // Переменная для случайного шанса на размножение
            if (stats.getCount() < 10) {
                Random random = new Random();
                luck = random.nextInt(0, 1); // Если популяция мала, вероятность снижается
            }
            // Создаем новых существ в экосистеме
            processor.createCreature(getConsumersCategory(), consumer.toUpperCase(), birthCount * luck);
            String message = consumer + "S have reproduced " + birthCount + " " + consumer + "S";
            logger.info(message);
            System.out.println(message); // Вывод сообщения для пользователя
        } else {
            // Если условия для размножения не выполнены, выводим предупреждение
            String message = "There is no such " + consumer + " in the ecosystem or not enough population to reproduce";
            logger.warn(message);
            System.out.println(message); // Вывод сообщения для пользователя
        }
    }
}
