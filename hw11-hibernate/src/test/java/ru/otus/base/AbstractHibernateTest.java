package ru.otus.base;

import static ru.otus.demo.DbServiceDemo.HIBERNATE_CFG_FILE;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.otus.core.repository.DataTemplateHibernate;
import ru.otus.core.repository.HibernateUtils;
import ru.otus.core.sessionmanager.TransactionManagerHibernate;
import ru.otus.crm.dbmigrations.MigrationsExecutorFlyway;
import ru.otus.crm.model.Address;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;
import ru.otus.crm.service.DBServiceClient;
import ru.otus.crm.service.DbServiceClientImpl;

public abstract class AbstractHibernateTest {
    protected SessionFactory sessionFactory;
    protected TransactionManagerHibernate transactionManager;
    protected DataTemplateHibernate<Client> clientTemplate;
    protected DBServiceClient dbServiceClient;

    private TestContainersConfig.CustomPostgreSQLContainer container;

    @BeforeEach
    void setUp() {
        container = new TestContainersConfig.CustomPostgreSQLContainer();
        container.start();

        String dbUrl = container.getJdbcUrl();
        String dbUserName = container.getUsername();
        String dbPassword = container.getPassword();

        new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword).executeMigrations();

        Configuration configuration = new Configuration().configure(HIBERNATE_CFG_FILE);
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.username", dbUserName);
        configuration.setProperty("hibernate.connection.password", dbPassword);

        sessionFactory = HibernateUtils.buildSessionFactory(configuration, Client.class, Address.class, Phone.class);

        transactionManager = new TransactionManagerHibernate(sessionFactory);
        clientTemplate = new DataTemplateHibernate<>(Client.class);
        dbServiceClient = new DbServiceClientImpl(transactionManager, clientTemplate);
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null) sessionFactory.close();
        if (container != null) container.stop();
    }
}
