package ru.otus.auth;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.otus.model.User;
import ru.otus.service.UserService;

@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserService service;

  public AuthServiceImpl(UserService service) {
    this.service = service;
  }

  @Override
  public boolean authenticate(String login, String password) {
    try {
      Optional<User> userOptional = service.findByLogin(login);

      if (userOptional.isEmpty()) {
        log.warn("User not found: {}", login);
        return false;
      }

      User user = userOptional.get();
      boolean authenticated = user.getPassword().equals(password);

      if (!authenticated) {
        log.warn("Invalid password for user: {}", login);
      }

      return authenticated;
    } catch (Exception e) {
      log.error("Authentication error for user: {}", login, e);
      return false;
    }
  }
}
