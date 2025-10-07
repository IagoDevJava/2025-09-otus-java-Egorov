package ru.otus;

import com.google.common.base.Preconditions;
import lombok.Data;

@Data
public class HelloOtus {

    private HelloOtus() {}

    public static boolean checkArgument() {
        Preconditions.checkArgument(true);
        return false;
    }
}
