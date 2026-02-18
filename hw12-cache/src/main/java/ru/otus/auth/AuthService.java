package ru.otus.auth;

public interface AuthService {

  boolean authenticate(String login, String password);
}
