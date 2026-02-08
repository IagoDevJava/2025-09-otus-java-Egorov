package ru.otus.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cache.HwCache;
import ru.otus.model.Client;
import ru.otus.repository.DataTemplate;
import ru.otus.sessionmanager.TransactionManager;

public class DbServiceClientImpl implements DBServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DbServiceClientImpl.class);

    private final DataTemplate<Client> clientDataTemplate;
    private final TransactionManager transactionManager;
    private final HwCache<Long, Client> cache;
    private final boolean enableCache;

    public DbServiceClientImpl(
            TransactionManager transactionManager,
            DataTemplate<Client> clientDataTemplate,
            HwCache<Long, Client> cache) {
        this(transactionManager, clientDataTemplate, true, cache);
    }

    public DbServiceClientImpl(
            TransactionManager transactionManager,
            DataTemplate<Client> clientDataTemplate,
            boolean enableCache,
            HwCache<Long, Client> cache) {
        this.transactionManager = transactionManager;
        this.clientDataTemplate = clientDataTemplate;
        this.enableCache = enableCache;
        this.cache = cache;

        if (enableCache) {
            log.info("Cache enabled for DbServiceClient");
        }
    }

    @Override
    public Client saveClient(Client client) {
        long startTime = System.nanoTime();

        Client savedClient = transactionManager.doInTransaction(session -> {
            var clientCloned = client.clone();
            if (client.getId() == null) {
                var insertedClient = clientDataTemplate.insert(session, clientCloned);
                log.info("created client: {}", clientCloned);
                return insertedClient;
            }
            var updatedClient = clientDataTemplate.update(session, clientCloned);
            log.info("updated client: {}", updatedClient);
            return updatedClient;
        });

        if (enableCache && savedClient != null) {
            cache.put(savedClient.getId(), savedClient);
            log.debug("Cached client with id: {}", savedClient.getId());
        }

        long endTime = System.nanoTime();
        log.debug("saveClient execution time: {} ns", (endTime - startTime));

        return savedClient;
    }

    @Override
    public Optional<Client> getClient(long id) {
        long startTime = System.nanoTime();
        String source = "DATABASE";

        if (enableCache) {
            Client cachedClient = cache.get(id);
            if (cachedClient != null) {
                source = "CACHE";
                long endTime = System.nanoTime();
                log.debug("getClient from {} execution time: {} ns (id: {})", source, (endTime - startTime), id);
                return Optional.of(cachedClient);
            }
        }

        Optional<Client> clientOptional = transactionManager.doInReadOnlyTransaction(session -> {
            var optional = clientDataTemplate.findById(session, id);
            log.debug("client from DB: {}", optional);
            return optional;
        });

        if (enableCache && clientOptional.isPresent()) {
            cache.put(id, clientOptional.get());
            log.debug("Added client to cache with id: {}", id);
        }

        long endTime = System.nanoTime();
        log.debug("getClient from {} execution time: {} ns (id: {})", source, (endTime - startTime), id);

        return clientOptional;
    }

    @Override
    public List<Client> findAll() {
        long startTime = System.nanoTime();

        List<Client> clients = transactionManager.doInReadOnlyTransaction(session -> {
            var clientList = clientDataTemplate.findAll(session);
            log.debug("clientList from DB, size: {}", clientList.size());
            return clientList;
        });

        long endTime = System.nanoTime();
        log.debug("findAll execution time: {} ns", (endTime - startTime));

        return clients;
    }

    public void clearCache() {
        if (enableCache) {
            cache.clear();
            log.info("Cache cleared");
        }
    }

    public int getCacheSize() {
        return enableCache ? cache.size() : 0;
    }
}
