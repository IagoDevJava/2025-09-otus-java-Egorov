package ru.otus.testframework.core;

import ch.qos.logback.classic.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import ru.otus.testframework.annotations.After;
import ru.otus.testframework.annotations.Before;
import ru.otus.testframework.annotations.Test;
import ru.otus.testframework.exception.TestFrameworkException;

public final class TestRunner {

    public TestRunner() {}

    private static final Logger log = (Logger) LoggerFactory.getLogger(TestRunner.class);

    public static void runTests(Class<?> testClass) {
        TestStatistics statistics = new TestStatistics();

        List<Method> testMethods = findMethodsAnnotatedWith(testClass, Test.class);
        List<Method> beforeMethods = findMethodsAnnotatedWith(testClass, Before.class);
        List<Method> afterMethods = findMethodsAnnotatedWith(testClass, After.class);

        validateMethods(testClass, beforeMethods, afterMethods, testMethods);

        executeTestMethods(testClass, testMethods, beforeMethods, afterMethods, statistics);

        statistics.printStatistics();
    }

    private static List<Method> findMethodsAnnotatedWith(Class<?> testClass, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                method.setAccessible(true);
                methods.add(method);
            }
        }
        return methods;
    }

    private static void validateMethods(
            Class<?> testClass, List<Method> beforeMethods, List<Method> afterMethods, List<Method> testMethods) {
        for (Method method : beforeMethods) {
            validateLifecycleMethod(method, "Before");
        }

        for (Method method : afterMethods) {
            validateLifecycleMethod(method, "After");
        }

        for (Method method : testMethods) {
            validateTestMethod(method);
        }

        if (testMethods.isEmpty()) {
            log.warn("В классе {} не найдено тестовых методов с аннотацией @Test", testClass.getSimpleName());
        } else {
            log.info(
                    "Найдено тестовых методов: {}, Before методов: {}, After методов: {}",
                    testMethods.size(),
                    beforeMethods.size(),
                    afterMethods.size());
        }
    }

    private static void validateLifecycleMethod(Method method, String methodType) {
        if (method.getParameterCount() > 0) {
            String errorMessage =
                    String.format("Метод @%s '%s' не должен иметь параметров", methodType, method.getName());
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static void validateTestMethod(Method method) {
        if (method.getParameterCount() > 0) {
            String errorMessage = String.format("Тестовый метод '%s' не должен иметь параметров", method.getName());
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static void executeTestMethods(
            Class<?> testClass,
            List<Method> testMethods,
            List<Method> beforeMethods,
            List<Method> afterMethods,
            TestStatistics statistics) {
        for (Method testMethod : testMethods) {
            executeSingleTest(testClass, testMethod, beforeMethods, afterMethods, statistics);
        }
    }

    private static void executeSingleTest(
            Class<?> testClass,
            Method testMethod,
            List<Method> beforeMethods,
            List<Method> afterMethods,
            TestStatistics statistics) {
        Object testInstance;
        try {
            testInstance = testClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Не удалось создать экземпляр тестового класса %s: %s", testClass.getSimpleName(), e.getMessage());
            log.error(errorMessage);
            throw new TestFrameworkException(errorMessage, e);
        }

        TestResult testResult = executeTestWithLifecycle(testInstance, testMethod, beforeMethods, afterMethods);
        statistics.addResult(testResult);
    }

    private static TestResult executeTestWithLifecycle(
            Object testInstance, Method testMethod, List<Method> beforeMethods, List<Method> afterMethods) {
        long startTime = System.currentTimeMillis();
        Throwable testException = null;

        try {
            executeLifecycleMethods(testInstance, beforeMethods, "Before");

            testMethod.invoke(testInstance);

        } catch (Exception e) {
            testException = e.getCause() != null ? e.getCause() : e;
        } finally {
            executeLifecycleMethods(testInstance, afterMethods, "After");
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new TestResult(testMethod, testException == null, testException, executionTime);
    }

    private static void executeLifecycleMethods(Object testInstance, List<Method> methods, String methodType) {

        for (Method method : methods) {
            try {
                method.invoke(testInstance);

            } catch (Exception e) {
                Throwable realException = e.getCause() != null ? e.getCause() : e;
                log.error(
                        "Ошибка в @{} методе '{}': {}",
                        methodType,
                        method.getName(),
                        realException.getMessage(),
                        realException);
            }
        }
    }
}
