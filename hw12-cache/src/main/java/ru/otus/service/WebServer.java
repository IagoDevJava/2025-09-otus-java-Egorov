package ru.otus.service;

@SuppressWarnings({"squid:S112"})
public interface WebServer {

  void start() throws Exception;

  void join() throws Exception;
}
