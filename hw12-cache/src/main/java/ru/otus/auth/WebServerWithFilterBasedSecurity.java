package ru.otus.auth;

import java.util.Arrays;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import ru.otus.service.ClientService;
import ru.otus.service.TemplateProcessor;
import ru.otus.service.WebServerSimple;

public class WebServerWithFilterBasedSecurity extends WebServerSimple {

  private final AuthService authService;

  public WebServerWithFilterBasedSecurity(
      int port, AuthService authService, ClientService clientService,
      TemplateProcessor templateProcessor) {
    super(port, clientService, templateProcessor);
    this.authService = authService;
  }

  @Override
  protected Handler applySecurity(ServletContextHandler servletContextHandler, String... paths) {
    servletContextHandler.addServlet(
        new ServletHolder(new LoginServlet(templateProcessor, authService)), "/login");
    AuthorizationFilter authorizationFilter = new AuthorizationFilter();
    Arrays.stream(paths)
        .forEachOrdered(
            path -> servletContextHandler.addFilter(new FilterHolder(authorizationFilter), path,
                null));
    return servletContextHandler;
  }
}
