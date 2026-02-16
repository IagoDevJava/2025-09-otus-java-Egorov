package ru.otus.repository;

import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.otus.model.User;

public class DataTemplateHibernate<T> implements DataTemplate<T> {

  private final Class<T> clazz;

  public DataTemplateHibernate(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public Optional<T> findById(Session session, long id) {
    return Optional.ofNullable(session.find(clazz, id));
  }

  @Override
  public Optional<T> findByLogin(Session session, String login) {
    String hql = "SELECT u FROM User u WHERE u.login = :login";

    Query<User> query = session.createQuery(hql, User.class);
    query.setParameter("login", login);

    return (Optional<T>) query.uniqueResultOptional();
  }

  @Override
  public List<T> findAll(Session session) {
    return session.createQuery(String.format("from %s", clazz.getSimpleName()), clazz)
        .getResultList();
  }

  @Override
  public T insert(Session session, T object) {
    session.persist(object);
    session.flush();
    return object;
  }

  @Override
  public T update(Session session, T object) {
    return session.merge(object);
  }
}
