package ru.otus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import lombok.SneakyThrows;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.auth.AuthService;
import ru.otus.auth.AuthServiceImpl;
import ru.otus.auth.WebServerWithFilterBasedSecurity;
import ru.otus.cache.HwCache;
import ru.otus.cache.MyCache;
import ru.otus.dbmigrations.MigrationsExecutorFlyway;
import ru.otus.model.Address;
import ru.otus.model.Client;
import ru.otus.model.Phone;
import ru.otus.model.User;
import ru.otus.model.UserDto;
import ru.otus.repository.DataTemplateHibernate;
import ru.otus.repository.HibernateUtils;
import ru.otus.service.ClientServiceImpl;
import ru.otus.service.TemplateProcessor;
import ru.otus.service.TemplateProcessorImpl;
import ru.otus.service.UserServiceImpl;
import ru.otus.service.WebServer;
import ru.otus.sessionmanager.TransactionManagerHibernate;

public class DbServiceDemo {

  private static final Logger log = LoggerFactory.getLogger(DbServiceDemo.class);
  public static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
  private static final int WEB_SERVER_PORT = 8081;
  private static final String TEMPLATES_DIR = "/templates/";
  public static final String DEFAULT_PASSWORD = "11111";

  public static void main(String[] args) {
    var configuration = new Configuration().configure(HIBERNATE_CFG_FILE);

    var dbUrl = configuration.getProperty("hibernate.connection.url");
    var dbUserName = configuration.getProperty("hibernate.connection.username");
    var dbPassword = configuration.getProperty("hibernate.connection.password");

    new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword).executeMigrations();

    var sessionFactory =
        HibernateUtils.buildSessionFactory(configuration, Client.class, Address.class, Phone.class,
            User.class);

    var transactionManager = new TransactionManagerHibernate(sessionFactory);
    var clientTemplate = new DataTemplateHibernate<>(Client.class);
    var userTemplate = new DataTemplateHibernate<>(User.class);
    HwCache<Long, Client> clientCache = new MyCache<>();
    HwCache<Long, User> userCache = new MyCache<>();

    var clientService = new ClientServiceImpl(transactionManager, clientTemplate, clientCache);
    var userService = new UserServiceImpl(transactionManager, userTemplate, userCache);

    log.info("=== Часть 1: Клиенты и тп ===");
    runOriginalDemo(clientService);

    log.info("=== Часть 1: Создание тестовых юзеров ===");
    runSavedUserDemo(userService);

    log.info("\n=== Часть 2: Подключение веб сервлета ===");
    demonstrateWeb(userService, clientService);
  }

  private static void runOriginalDemo(ClientServiceImpl service) {
    service.saveClient(new Client("dbServiceFirst"));

    var clientSecond = service.saveClient(new Client("dbServiceSecond"));
    var clientSecondSelected = service.getClient(clientSecond.getId())
        .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
    log.info("clientSecondSelected:{}", clientSecondSelected);

    service.saveClient(new Client(clientSecondSelected.getId(), "dbServiceSecondUpdated"));
    var clientUpdated = service.getClient(clientSecondSelected.getId())
        .orElseThrow(
            () -> new RuntimeException("Client not found, id:" + clientSecondSelected.getId()));
    log.info("clientUpdated:{}", clientUpdated);

    log.info("All clients");
    service.findAll().forEach(client -> log.info("client:{}", client));
  }

  private static void runSavedUserDemo(UserServiceImpl service) {
    User user1 = new User("Крис Гир", "user1", DEFAULT_PASSWORD);
    User user2 = new User("Ая Кэш", "user2", DEFAULT_PASSWORD);
    UserDto userDto1 = service.createUser(user1);
    UserDto userDto2 = service.createUser(user2);
    log.info("В базе два тестовых юзера:\n1: {};\n2: {};", userDto1, userDto2);

    List<UserDto> users = service.getUsers();

    log.info("Tестовые юзеры лежат в списке с размером: {}", users.size());
  }

  @SneakyThrows
  private static void demonstrateWeb(UserServiceImpl userService, ClientServiceImpl clientService) {
    Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    TemplateProcessor templateProcessor = new TemplateProcessorImpl(TEMPLATES_DIR);
    AuthService authService = new AuthServiceImpl(userService);

    WebServer webServer =
        new WebServerWithFilterBasedSecurity(WEB_SERVER_PORT, authService, clientService,
            templateProcessor);

    webServer.start();
    webServer.join();
  }
}
