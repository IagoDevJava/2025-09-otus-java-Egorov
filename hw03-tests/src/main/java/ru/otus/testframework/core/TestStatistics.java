package ru.otus.testframework.core;

import java.util.ArrayList;
import java.util.List;

public class TestStatistics {

    private final List<TestResult> results = new ArrayList<>();

    public void addResult(TestResult result) {
        results.add(result);
    }

    public int getTotalTests() {
        return results.size();
    }

    public long getPassedTests() {
        return results.stream().filter(TestResult::isPassed).count();
    }

    public long getFailedTests() {
        return results.stream().filter(result -> !result.isPassed()).count();
    }

    public void printStatistics() {
        System.out.println("\n=== TEST EXECUTION SUMMARY ===");
        System.out.printf("Total tests: %d%n", getTotalTests());
        System.out.printf("Passed: %d%n", getPassedTests());
        System.out.printf("Failed: %d%n", getFailedTests());
        System.out.println("==============================");

        for (TestResult result : results) {
            if (result.isPassed()) {
                System.out.printf("PASS - %s (%d ms)%n", result.getTestMethod().getName(), result.getExecutionTime());
            } else {
                System.out.printf("FAIL - %s (%d ms)%n", result.getTestMethod().getName(), result.getExecutionTime());
            }
        }
    }
}
