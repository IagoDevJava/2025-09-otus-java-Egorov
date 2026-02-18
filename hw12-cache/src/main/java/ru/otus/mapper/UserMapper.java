package ru.otus.mapper;

import java.util.ArrayList;
import java.util.List;
import ru.otus.model.User;
import ru.otus.model.UserDto;

public class UserMapper {

  public static UserDto mapUserToUserDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .name(user.getName())
        .login(user.getLogin())
        .build();
  }

  public static List<UserDto> mapListUserDto(List<User> users) {
    List<UserDto> userDtos = new ArrayList<>();
    for (User user : users) {
      userDtos.add(mapUserToUserDto(user));
    }
    return userDtos;
  }
}
