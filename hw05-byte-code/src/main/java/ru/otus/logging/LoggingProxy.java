package ru.otus.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ru.otus.annotation.MyLog;

public class LoggingProxy implements InvocationHandler {

  private final Object clazz;
  private final ConcurrentMap<Method, Boolean> methodLoggingCache = new ConcurrentHashMap<>();

  private LoggingProxy(Object clazz) {
    this.clazz = clazz;
  }

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(T target) {
    return (T) Proxy.newProxyInstance(
        target.getClass().getClassLoader(), target.getClass().getInterfaces(),
        new LoggingProxy(target));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Boolean shouldLog = methodLoggingCache.computeIfAbsent(method, m -> {
      try {
        Method clazzMethod = clazz.getClass().getMethod(m.getName(), m.getParameterTypes());
        return clazzMethod.isAnnotationPresent(MyLog.class);
      } catch (NoSuchMethodException e) {
        return false;
      }
    });

    if (shouldLog) {
      logMethodCall(method, args);
    }

    return method.invoke(clazz, args);
  }

  private void logMethodCall(Method method, Object[] args) {
    StringJoiner paramJoiner = new StringJoiner(", ");
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        paramJoiner.add("param" + (i + 1) + ": " + args[i]);
      }
    }
    System.out.println("executed method: " + method.getName() + ", " + paramJoiner);
  }
}
