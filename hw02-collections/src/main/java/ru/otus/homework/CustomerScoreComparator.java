package ru.otus.homework;

import java.util.Comparator;

public class CustomerScoreComparator implements Comparator<Customer> {

    @Override
    public int compare(Customer c1, Customer c2) {
        int scoreCompare = Long.compare(c1.getScores(), c2.getScores());
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Long.compare(c1.getId(), c2.getId());
    }
}
