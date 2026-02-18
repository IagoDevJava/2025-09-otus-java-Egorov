package ru.otus.service;

import java.util.List;
import java.util.Optional;
import ru.otus.model.User;
import ru.otus.model.UserDto;

public interface UserService {

  List<UserDto> getUsers();

  UserDto createUser(User user);

  Optional<User> findByLogin(String login);
}
