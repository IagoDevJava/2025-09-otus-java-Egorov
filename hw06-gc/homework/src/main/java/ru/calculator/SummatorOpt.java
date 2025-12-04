package ru.calculator;

import java.security.SecureRandom;
import java.util.ArrayList;

public class SummatorOpt {

    private int sum = 0;
    private int prevValue = 0;
    private int prevPrevValue = 0;
    private int sumLastThreeValues = 0;
    private int someValue = 0;
    // !!! эта коллекция должна остаться. Заменять ее на счетчик нельзя.
    private final ArrayList<DataOpt> listValues = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    // !!! сигнатуру метода менять нельзя
    public void calc(DataOpt data) {
        listValues.add(data);
        if (listValues.size() % 100_000 == 0) {
            listValues.clear();
            listValues.trimToSize();
        }
        sum += data.getValue() + random.nextInt();

        sumLastThreeValues = data.getValue() + prevValue + prevPrevValue;

        prevPrevValue = prevValue;
        prevValue = data.getValue();

        for (var idx = 0; idx < 3; idx++) {
            someValue += (sumLastThreeValues * sumLastThreeValues / (data.getValue() + 1) - sum);
            someValue = Math.abs(someValue) + listValues.size();
        }
    }

    public int getSum() {
        return sum;
    }

    public int getPrevValue() {
        return prevValue;
    }

    public int getPrevPrevValue() {
        return prevPrevValue;
    }

    public int getSumLastThreeValues() {
        return sumLastThreeValues;
    }

    public int getSomeValue() {
        return someValue;
    }
}
