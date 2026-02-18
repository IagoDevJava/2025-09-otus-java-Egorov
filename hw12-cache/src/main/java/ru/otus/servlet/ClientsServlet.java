package ru.otus.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ru.otus.model.Address;
import ru.otus.model.Client;
import ru.otus.model.Phone;
import ru.otus.service.ClientService;
import ru.otus.service.TemplateProcessor;

@SuppressWarnings({"java:S1989"})
@Slf4j
public class ClientsServlet extends HttpServlet {

  private static final String CLIENTS_PAGE_TEMPLATE = "clients.html";

  private final ClientService clientService;
  private final TemplateProcessor templateProcessor;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ClientsServlet(TemplateProcessor templateProcessor, ClientService clientService) {
    this.templateProcessor = templateProcessor;
    this.clientService = clientService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
    List<Client> clients = clientService.findAll();

    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put("clients", clients);

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(templateProcessor.getPage(CLIENTS_PAGE_TEMPLATE, paramsMap));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // Читаем и парсим тело запроса
      String body = req.getReader().lines().collect(Collectors.joining());
      log.debug("Received client creation request: {}", body);

      // Парсим JSON в Map для гибкости
      Map<String, Object> data = objectMapper.readValue(body, Map.class);

      String name = (String) data.get("name");
      Map<String, Object> addressData = (Map<String, Object>) data.get("address");
      List<Map<String, Object>> phonesData = (List<Map<String, Object>>) data.get("phones");

      // Валидация обязательных полей
      if (name == null || name.trim().isEmpty()) {
        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Имя клиента обязательно");
        return;
      }

      // Создаём объект Address (если есть данные)
      Address address = null;
      if (addressData != null) {
        String street = (String) addressData.get("street");
        if (street != null && !street.trim().isEmpty()) {
          address = new Address(street);
        }
      }

      // Создаём список Phone (если есть данные)
      List<Phone> phones = null;
      if (phonesData != null && !phonesData.isEmpty()) {
        phones = phonesData.stream()
            .map(phoneMap -> {
              String number = (String) phoneMap.get("number");
              return number != null && !number.trim().isEmpty() ? new Phone(number) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }

      // Создаём клиента через фабричный метод
      Client client = Client.create(name, address, phones);
      Client createdClient = clientService.saveClient(client);

      if (createdClient == null) {
        throw new RuntimeException("Не удалось создать клиента");
      }

      // Возвращаем успешный ответ
      sendSuccessResponse(resp, createdClient);

      log.info(
          "Client created successfully: id={}, name={}, address={}, phonesCount={}",
          createdClient.getId(),
          createdClient.getName(),
          createdClient.getAddress(),
          createdClient.getPhones().size());

    } catch (Exception e) {
      log.error("Error creating client", e);
      sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Ошибка сервера: " + e.getMessage());
    }
  }

  private void sendSuccessResponse(HttpServletResponse resp, Client client) throws IOException {
    resp.setStatus(HttpServletResponse.SC_CREATED);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    Map<String, Object> responseData = new HashMap<>();
    responseData.put("success", true);
    responseData.put("message", "Клиент успешно создан");

    Map<String, Object> clientData = new HashMap<>();
    clientData.put("id", client.getId() != null ? client.getId() : "N/A");
    clientData.put("name", client.getName());

    if (client.getAddress() != null) {
      Map<String, Object> addressData = new HashMap<>();
      addressData.put(
          "id",
          client.getAddress().getId() != null ? client.getAddress().getId() : "N/A");
      addressData.put("street", client.getAddress().getStreet());
      clientData.put("address", addressData);
    }

    List<Map<String, Object>> phonesData = client.getPhones().stream()
        .map(phone -> {
          Map<String, Object> phoneMap = new HashMap<>();
          phoneMap.put("id", phone.getId() != null ? phone.getId() : "N/A");
          phoneMap.put("number", phone.getNumber());
          return phoneMap;
        })
        .collect(Collectors.toList());
    clientData.put("phones", phonesData);

    responseData.put("client", clientData);

    resp.getWriter().write(objectMapper.writeValueAsString(responseData));
  }

  private void sendErrorResponse(HttpServletResponse resp, int status, String message)
      throws IOException {
    resp.setStatus(status);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter()
        .write(objectMapper.writeValueAsString(Map.of("success", false, "error", message)));
  }
}
