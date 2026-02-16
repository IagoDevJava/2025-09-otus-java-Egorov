package ru.otus.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cache.HwCache;
import ru.otus.mapper.UserMapper;
import ru.otus.model.User;
import ru.otus.model.UserDto;
import ru.otus.repository.DataTemplate;
import ru.otus.sessionmanager.TransactionManager;

public class UserServiceImpl implements UserService {

  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

  private final DataTemplate<User> usertDataTemplate;
  private final TransactionManager transactionManager;
  private final HwCache<Long, User> userCache;
  private final boolean enableCache;

  public UserServiceImpl(
      TransactionManager transactionManager,
      DataTemplate<User> usertDataTemplate,
      HwCache<Long, User> userCache) {
    this(transactionManager, usertDataTemplate, true, userCache);
  }

  public UserServiceImpl(
      TransactionManager transactionManager,
      DataTemplate<User> usertDataTemplate,
      boolean enableCache,
      HwCache<Long, User> userCache) {
    this.transactionManager = transactionManager;
    this.usertDataTemplate = usertDataTemplate;
    this.enableCache = enableCache;
    this.userCache = userCache;

    if (enableCache) {
      log.info("Cache enabled for UserService");
    }
  }

  @Override
  public List<UserDto> getUsers() {
    long startTime = System.nanoTime();

    List<User> clients = transactionManager.doInReadOnlyTransaction(session -> {
      var userList = usertDataTemplate.findAll(session);
      log.debug("userList from DB, size: {}", userList.size());
      return userList;
    });

    long endTime = System.nanoTime();
    log.debug("findAllUser execution time: {} ns", (endTime - startTime));

    return UserMapper.mapListUserDto(clients);
  }

  @Override
  public UserDto createUser(User user) {
    long startTime = System.nanoTime();

    User savedNewUser = transactionManager.doInTransaction(session -> {
      var userCloned = user.clone();
      if (user.getId() == null) {
        var insertedUser = usertDataTemplate.insert(session, userCloned);
        log.info("created user: {}", userCloned);
        return insertedUser;
      }
      var updatedClient = usertDataTemplate.update(session, userCloned);
      log.info("updated user: {}", updatedClient);
      return updatedClient;
    });

    if (enableCache && savedNewUser != null) {
      userCache.put(savedNewUser.getId(), savedNewUser);
      log.debug("Cached user with id: {}", savedNewUser.getId());
    }

    long endTime = System.nanoTime();
    log.debug("saveUser execution time: {} ns", (endTime - startTime));

    if (savedNewUser != null) {
      return UserMapper.mapUserToUserDto(savedNewUser);
    } else {
      return null;
    }
  }

  @Override
  public Optional<User> findByLogin(String login) {
    long startTime = System.nanoTime();

    Optional<User> optionalUserByLogin = transactionManager.doInReadOnlyTransaction(session -> {
      Optional<User> userbylogin = usertDataTemplate.findByLogin(session, login);

      userbylogin.ifPresent(
          user -> log.debug("User with login {} from DB: {}", login, user.getLogin()));

      return userbylogin;
    });

    long endTime = System.nanoTime();
    log.debug("findUserByLogin execution time: {} ns", (endTime - startTime));

    return optionalUserByLogin;
  }
}
