package ru.otus.testframework.example;

import ru.otus.testframework.annotations.After;
import ru.otus.testframework.annotations.Before;
import ru.otus.testframework.annotations.Test;
import ru.otus.testframework.core.TestRunner;

public class ExampleTest {

    private int setupCounter = 0;
    private final String instanceId;
    private final StringBuilder executionLog;

    @SuppressWarnings("this-escape")
    public ExampleTest() {
        this.instanceId = "Instance_" + System.identityHashCode(this);
        this.executionLog = new StringBuilder();
        log("Constructor called");
    }

    @Before
    private void setUp() {
        setupCounter++;
        log("@Before method: test setup #" + setupCounter);
        executionLog.setLength(0);
    }

    @After
    private void tearDown() {
        log("@After method: cleanup after test");
    }

    @Test
    public void testSuccessfulAddition() {
        log("@Test testSuccessfulAddition: addition test");
        int result = 2 + 2;

        log("Addition works correctly: 2 + 2 = " + result);
    }

    @Test
    public void testStringOperations() {
        log("@Test testStringOperations: string operations test");

        String text = "Hello, Test Framework!";
        String upperCase = text.toUpperCase();

        if (!upperCase.contains("FRAMEWORK")) {
            throw new AssertionError("String should contain 'FRAMEWORK'");
        }

        log("String operations work: " + upperCase);
    }

    @Test
    public void testWithCustomException() {
        log("@Test testWithCustomException: test with custom exception");
        throw new IllegalStateException("Custom error: something went wrong in business logic");
    }

    @Test
    public void testVerification() {
        log("@Test testVerification: test isolation verification");
        log("setupCounter = " + setupCounter + " (should be 1 for isolated test)");

        if (setupCounter != 1) {
            throw new AssertionError("Test isolation violated! setupCounter = " + setupCounter + ", expected 1");
        }

        log("Test isolation works correctly");
    }

    @Before
    private void additionalSetup() {
        log("@Before additionalSetup: additional setup");
    }

    @After
    private void additionalCleanup() {
        log("@After additionalCleanup: additional cleanup");
    }

    public void helperMethod() {
        log("Helper method (not a test)");
    }

    @Test
    private void testPrivateMethod() {
        log("@Test testPrivateMethod: private test method");
        log("Private methods supported via Reflection");
    }

    private void log(String message) {
        String fullMessage = String.format("[%s] %s", instanceId, message);
        System.out.println(fullMessage);
        executionLog.append(fullMessage).append("\n");
    }

    public static void main(String[] args) {
        System.out.println("LAUNCHING TEST FRAMEWORK");
        System.out.println("================================");

        TestRunner.runTests(ExampleTest.class);

        System.out.println("================================");
        System.out.println("DEMONSTRATION COMPLETED");
    }
}
