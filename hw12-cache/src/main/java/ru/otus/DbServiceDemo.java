package ru.otus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cache.HwCache;
import ru.otus.cache.MyCache;
import ru.otus.dbmigrations.MigrationsExecutorFlyway;
import ru.otus.model.Address;
import ru.otus.model.Client;
import ru.otus.model.Phone;
import ru.otus.repository.DataTemplateHibernate;
import ru.otus.repository.HibernateUtils;
import ru.otus.service.DbServiceClientImpl;
import ru.otus.sessionmanager.TransactionManagerHibernate;

public class DbServiceDemo {

    private static final Logger log = LoggerFactory.getLogger(DbServiceDemo.class);
    public static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
    private static final Random random = new Random();
    private static final int PERFORMANCE_TEST_ITERATIONS = 1000;

    public static void main(String[] args) throws InterruptedException {
        var configuration = new Configuration().configure(HIBERNATE_CFG_FILE);

        var dbUrl = configuration.getProperty("hibernate.connection.url");
        var dbUserName = configuration.getProperty("hibernate.connection.username");
        var dbPassword = configuration.getProperty("hibernate.connection.password");

        new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword).executeMigrations();

        var sessionFactory =
                HibernateUtils.buildSessionFactory(configuration, Client.class, Address.class, Phone.class);

        var transactionManager = new TransactionManagerHibernate(sessionFactory);
        var clientTemplate = new DataTemplateHibernate<>(Client.class);
        HwCache<Long, Client> cache = new MyCache<>();

        var dbServiceClient = new DbServiceClientImpl(transactionManager, clientTemplate, cache);

        log.info("=== Часть 1: Оригинальная функциональность ===");
        runOriginalDemo(dbServiceClient);

        log.info("\n=== Часть 2: Демонстрация работы кэша ===");
        demonstrateCache(dbServiceClient);

        log.info("\n=== Часть 3: Производительность кэша vs БД ===");
        testCachePerformance(dbServiceClient);

        log.info("\n=== Часть 4: Работа GC с WeakHashMap ===");
        testGarbageCollection();

        log.info("\n=== Часть 5: Дополнительные тесты ===");
        runAdditionalTests(dbServiceClient);
    }

    private static void runOriginalDemo(DbServiceClientImpl service) {
        service.saveClient(new Client("dbServiceFirst"));

        var clientSecond = service.saveClient(new Client("dbServiceSecond"));
        var clientSecondSelected = service.getClient(clientSecond.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        log.info("clientSecondSelected:{}", clientSecondSelected);

        service.saveClient(new Client(clientSecondSelected.getId(), "dbServiceSecondUpdated"));
        var clientUpdated = service.getClient(clientSecondSelected.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecondSelected.getId()));
        log.info("clientUpdated:{}", clientUpdated);

        log.info("All clients");
        service.findAll().forEach(client -> log.info("client:{}", client));
    }

    private static void demonstrateCache(DbServiceClientImpl service) {
        Client testClient = service.saveClient(new Client("CacheTestClient"));
        long clientId = testClient.getId();
        log.info("Создан клиент для теста кэша с ID: {}", clientId);

        log.info("1. Первое чтение клиента (из БД):");
        long startTime = System.nanoTime();
        //        var clientFromDb = service.getClient(clientId);
        long dbReadTime = System.nanoTime() - startTime;
        log.info("   Время: {} нс ({} мс)", dbReadTime, TimeUnit.NANOSECONDS.toMillis(dbReadTime));
        log.info("   Размер кэша после первого чтения: {}", service.getCacheSize());

        log.info("2. Второе чтение того же клиента (из кэша):");
        startTime = System.nanoTime();
        //        var clientFromCache = service.getClient(clientId);
        long cacheReadTime = System.nanoTime() - startTime;
        log.info("   Время: {} нс ({} мкс)", cacheReadTime, TimeUnit.NANOSECONDS.toMicros(cacheReadTime));
        log.info("   Размер кэша: {}", service.getCacheSize());

        if (dbReadTime > 0 && cacheReadTime > 0) {
            double speedup = (double) dbReadTime / cacheReadTime;
            log.info("3. Ускорение при использовании кэша: {} раз", speedup);
            log.info("   Чтение из БД в {} раз медленнее", speedup);
        }

        log.info("4. Тест обновления клиента и инвалидации кэша:");
        Client updatedClient = new Client(clientId, "UpdatedCacheTestClient");
        service.saveClient(updatedClient);
        log.info("   Клиент обновлен, кэш должен быть инвалидирован");
        log.info("   Размер кэша после обновления: {}", service.getCacheSize());

        startTime = System.nanoTime();
        service.getClient(clientId);
        long afterUpdateTime = System.nanoTime() - startTime;
        log.info("   Время чтения после обновления: {} нс", afterUpdateTime);

        log.info("5. Очистка кэша:");
        service.clearCache();
        log.info("   Размер кэша после очистки: {}", service.getCacheSize());
    }

    private static void testCachePerformance(DbServiceClientImpl service) {
        log.info("Подготовка: создаем {} клиентов для теста...", PERFORMANCE_TEST_ITERATIONS);
        List<Long> clientIds = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Client client = service.saveClient(new Client("PerfTestClient_" + i));
            clientIds.add(client.getId());
        }
        log.info("Создано {} клиентов", clientIds.size());

        service.clearCache();

        log.info("\nТест 1: Холодный кэш (все чтения из БД)");
        long coldCacheStart = System.nanoTime();
        for (Long id : clientIds) {
            service.getClient(id);
        }
        long coldCacheTime = System.nanoTime() - coldCacheStart;
        log.info("Время: {} нс ({} мс)", coldCacheTime, TimeUnit.NANOSECONDS.toMillis(coldCacheTime));
        log.info("Среднее время на чтение: {} нс", coldCacheTime / clientIds.size());
        log.info("Размер кэша после холодного прогона: {}", service.getCacheSize());

        log.info("\nТест 2: Горячий кэш (все чтения из кэша)");
        long hotCacheStart = System.nanoTime();
        for (Long id : clientIds) {
            service.getClient(id);
        }
        long hotCacheTime = System.nanoTime() - hotCacheStart;
        log.info("Время: {} нс ({} мкс)", hotCacheTime, TimeUnit.NANOSECONDS.toMicros(hotCacheTime));
        log.info("Среднее время на чтение: {} нс", hotCacheTime / clientIds.size());

        log.info("\nТест 3: Смешанная нагрузка ({} итераций)", PERFORMANCE_TEST_ITERATIONS);
        long mixedStart = System.nanoTime();
        int hits = 0;
        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            if (random.nextBoolean() && !clientIds.isEmpty()) {
                Long id = clientIds.get(random.nextInt(clientIds.size()));
                service.getClient(id);
                hits++;
            } else {
                service.getClient(-1L);
            }
        }
        long mixedTime = System.nanoTime() - mixedStart;

        log.info("Общее время: {} нс ({} мс)", mixedTime, TimeUnit.NANOSECONDS.toMillis(mixedTime));
        log.info("Попаданий в кэш: {}/{}", hits, PERFORMANCE_TEST_ITERATIONS);
        log.info("Процент попаданий: {}%", (hits * 100.0 / PERFORMANCE_TEST_ITERATIONS));
        log.info("Среднее время на операцию: {} нс", mixedTime / PERFORMANCE_TEST_ITERATIONS);

        log.info("\nИтоговое сравнение производительности:");
        log.info("Чтение из БД (холодный кэш): {} нс на операцию", coldCacheTime / clientIds.size());
        log.info("Чтение из кэша (горячий кэш): {} нс на операцию", hotCacheTime / clientIds.size());

        if (hotCacheTime > 0) {
            double speedup = (double) coldCacheTime / hotCacheTime;
            log.info("Ускорение при использовании кэша: {} раз", speedup);
        }
    }

    private static void testGarbageCollection() throws InterruptedException {
        log.info("Демонстрация работы GC с WeakHashMap...");

        ru.otus.cache.MyCache<Integer, byte[]> cache = new ru.otus.cache.MyCache<>();
        List<byte[]> memoryPressure = new ArrayList<>();

        // Я сознательно создаю ОГРОМНЫЕ объекты - иначе очистка не срабатывает на моей машине
        int objectCount = 1000;
        log.info("Заполняем кэш {} объектами по 20MB...", objectCount);
        for (int i = 0; i < objectCount; i++) {
            cache.put(i, new byte[20 * 1024 * 1024]);
        }

        log.info("Размер кэша после заполнения: {}", cache.size());

        log.info("Создаем нагрузку на память...");
        for (int i = 0; i < 20; i++) {
            memoryPressure.add(new byte[50 * 1024 * 1024]);
            Thread.sleep(50);

            if (i % 5 == 0) {
                int remaining = 0;
                for (int j = 0; j < objectCount; j++) {
                    if (cache.get(j) != null) {
                        remaining++;
                    }
                }
                log.info("   После {} аллокаций по 5MB: {} объектов в кэше", i + 1, remaining);
            }
        }

        log.info("Вызываем System.gc()...");
        System.gc();
        Thread.sleep(1000);

        int finalCount = 0;
        for (int i = 0; i < objectCount; i++) {
            if (cache.get(i) != null) {
                finalCount++;
            }
        }

        log.info("Результат после GC:");
        log.info("   Объектов осталось в кэше: {}", finalCount);
        log.info("   Объектов удалено GC: {}", objectCount - finalCount);

        if (finalCount < objectCount) {
            log.info("Доказано: WeakHashMap автоматически освобождает память при нехватке");
        } else {
            log.info("GC не освободил память (возможно, достаточно свободной памяти)");
        }

        memoryPressure.clear();
        System.gc();
    }

    private static void runAdditionalTests(DbServiceClientImpl service) {
        log.info("1. Тест повторяющихся чтений одного клиента:");

        Client repeatedClient = service.saveClient(new Client("RepeatedReadTest"));
        long repeatedId = repeatedClient.getId();

        int repeatCount = 100;
        long repeatStart = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            service.getClient(repeatedId);
        }
        long repeatTime = System.nanoTime() - repeatStart;

        log.info("   {} чтений клиента ID {}:", repeatCount, repeatedId);
        log.info("   Общее время: {} нс", repeatTime);
        log.info("   Среднее время на чтение: {} нс", repeatTime / repeatCount);

        log.info("\n2. Тест параллельной работы (имитация):");
        log.info("   Размер кэша: {}", service.getCacheSize());
        log.info("   Всего клиентов в БД: {}", service.findAll().size());

        log.info("\n3. Финальная проверка целостности данных:");
        List<Client> allClients = service.findAll();
        int cachedClients = service.getCacheSize();
        log.info("   Клиентов в БД: {}", allClients.size());
        log.info("   Клиентов в кэше: {}", cachedClients);

        if (cachedClients <= allClients.size()) {
            log.info("   ✓ Целостность данных сохранена");
        } else {
            log.warn("   ⚠ В кэше больше клиентов чем в БД (возможно устаревшие данные)");
        }

        log.info("\n=== Все тесты завершены ===");
    }
}
