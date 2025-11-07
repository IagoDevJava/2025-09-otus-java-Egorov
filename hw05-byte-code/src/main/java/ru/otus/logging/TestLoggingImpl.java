package ru.otus.logging;

import ru.otus.annotation.MyLog;

public class TestLoggingImpl implements TestLogging {

    @MyLog
    @Override
    public void calculation(int param) {
        System.out.println("One param: " + param);
    }

    //    @MyLog
    @Override
    public void calculation(int param1, int param2) {
        System.out.println("Two params: " + param1 + ", " + param2);
    }

    @MyLog
    @Override
    public void calculation(int param1, int param2, String param3) {
        System.out.println("Three params: " + param1 + ", " + param2 + ", " + param3);
    }
}
