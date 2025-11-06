package ru.otus.testframework.core;

import java.lang.reflect.Method;

public class TestResult {

  private final Method testMethod;
  private final boolean passed;
  private final long executionTime;

  public Method getTestMethod() {
    return testMethod;
  }

  public boolean isPassed() {
    return passed;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public TestResult(Method testMethod, boolean passed, Throwable exception, long executionTime) {
    this.testMethod = testMethod;
    this.passed = passed;
    this.executionTime = executionTime;
  }
}
