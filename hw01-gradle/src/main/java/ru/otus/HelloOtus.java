package ru.otus;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class HelloOtus {

    private HelloOtus() {}

    public static void checkArgument() {
        Preconditions.checkArgument(true);
        log.info("Hello Otus");
    }
}
