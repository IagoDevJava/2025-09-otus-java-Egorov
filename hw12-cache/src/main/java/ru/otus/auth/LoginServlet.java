package ru.otus.auth;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import ru.otus.service.TemplateProcessor;

@SuppressWarnings({"java:S1989"})
public class LoginServlet extends HttpServlet {

  private static final String PARAM_LOGIN = "login";
  private static final String PARAM_PASSWORD = "password";
  private static final int MAX_INACTIVE_INTERVAL = 30;
  private static final String LOGIN_PAGE_TEMPLATE = "login.html";

  private final transient TemplateProcessor templateProcessor;
  private final transient AuthService authService;

  public LoginServlet(TemplateProcessor templateProcessor, AuthService authService) {
    this.authService = authService;
    this.templateProcessor = templateProcessor;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html");

    // Проверяем, есть ли параметр ошибки
    String error = request.getParameter("error");
    Map<String, Object> pageVariables = Collections.emptyMap();

    if ("true".equals(error)) {
      pageVariables = Map.of("error", "Неверный логин или пароль");
    }

    response.getWriter().println(templateProcessor.getPage(LOGIN_PAGE_TEMPLATE, pageVariables));
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String login = request.getParameter(PARAM_LOGIN);
    String password = request.getParameter(PARAM_PASSWORD);

    if (authService.authenticate(login, password)) {
      HttpSession session = request.getSession();
      session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
      response.sendRedirect("/clients");
    } else {
      // Редирект обратно на логин с параметром ошибки
      response.sendRedirect("/login?error=true");
    }
  }
}
