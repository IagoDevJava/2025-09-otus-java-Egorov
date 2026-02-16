package ru.otus.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cache.HwCache;
import ru.otus.model.Client;
import ru.otus.repository.DataTemplate;
import ru.otus.sessionmanager.TransactionManager;

public class ClientServiceImpl implements ClientService {

  private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

  private final DataTemplate<Client> clientDataTemplate;
  private final TransactionManager transactionManager;
  private final HwCache<Long, Client> clientCache;
  private final boolean enableCache;

  public ClientServiceImpl(
      TransactionManager transactionManager,
      DataTemplate<Client> clientDataTemplate,
      HwCache<Long, Client> clientCache) {
    this(transactionManager, clientDataTemplate, true, clientCache);
  }

  public ClientServiceImpl(
      TransactionManager transactionManager,
      DataTemplate<Client> clientDataTemplate,
      boolean enableCache,
      HwCache<Long, Client> clientCache) {
    this.transactionManager = transactionManager;
    this.clientDataTemplate = clientDataTemplate;
    this.enableCache = enableCache;
    this.clientCache = clientCache;

    if (enableCache) {
      log.info("Cache enabled for ClientService");
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
        log.info("created client ID: {}", insertedClient.getId());
        return insertedClient;
      }
      var updatedClient = clientDataTemplate.update(session, clientCloned);
      log.info("updated client: {}", updatedClient);
      return updatedClient;
    });

    if (enableCache && savedClient != null) {
      clientCache.put(savedClient.getId(), savedClient);
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
      Client cachedClient = clientCache.get(id);
      if (cachedClient != null) {
        source = "CACHE";
        long endTime = System.nanoTime();
        log.debug("getClient from {} execution time: {} ns (id: {})", source, (endTime - startTime),
            id);
        return Optional.of(cachedClient);
      }
    }

    Optional<Client> clientOptional = transactionManager.doInReadOnlyTransaction(session -> {
      var optional = clientDataTemplate.findById(session, id);
      log.debug("client from DB: {}", optional);
      return optional;
    });

    if (enableCache && clientOptional.isPresent()) {
      clientCache.put(id, clientOptional.get());
      log.debug("Added client to cache with id: {}", id);
    }

    long endTime = System.nanoTime();
    log.debug("getClient from {} execution time: {} ns (id: {})", source, (endTime - startTime),
        id);

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
}
