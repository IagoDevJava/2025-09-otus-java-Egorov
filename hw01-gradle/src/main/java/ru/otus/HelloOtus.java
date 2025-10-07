package ru.otus;

import com.google.common.base.Preconditions;

public class HelloOtus {

    public static boolean checkArgument() {
        Preconditions.checkArgument(true);
        return false;
    }
}
