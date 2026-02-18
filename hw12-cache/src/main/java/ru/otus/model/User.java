package ru.otus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @SequenceGenerator(name = "users_gen", sequenceName = "users_seq", initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_gen")
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "login")
  private String login;

  @Column(name = "password")
  private String password;

  public User(Long id, String name, String login, String password) {
    this.id = id;
    this.name = name;
    this.login = login;
    this.password = password;
  }

  public User(String name, String login, String password) {
    this.name = name;
    this.login = login;
    this.password = password;
  }

  @Override
  @SuppressWarnings({"java:S2975", "java:S1182"})
  public User clone() {
    return new User(this.id, this.name, this.login, this.password);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(id, user.id)
        && Objects.equals(name, user.name)
        && Objects.equals(login, user.login)
        && Objects.equals(password, user.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, login, password);
  }
}
