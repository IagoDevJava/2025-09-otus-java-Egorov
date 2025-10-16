package ru.otus.homework;

import java.util.Map;
import java.util.TreeMap;

public class CustomerService {

    private final TreeMap<Customer, String> map = new TreeMap<>(new CustomerScoreComparator());

    public Map.Entry<Customer, String> getSmallest() {
        Map.Entry<Customer, String> entry = map.firstEntry();
        return entry != null ? copyEntry(entry) : null;
    }

    public Map.Entry<Customer, String> getNext(Customer customer) {
        if (customer == null) {
            return null;
        }
        Map.Entry<Customer, String> entry = map.higherEntry(customer);
        return entry != null ? copyEntry(entry) : null;
    }

    public void add(Customer customer, String data) {
        map.put(customer, data);
    }

    private Map.Entry<Customer, String> copyEntry(Map.Entry<Customer, String> entry) {
        return new Map.Entry<Customer, String>() {
            @Override
            public Customer getKey() {
                Customer original = entry.getKey();
                return new Customer(original.getId(), original.getName(), original.getScores());
            }

            @Override
            public String getValue() {
                return entry.getValue();
            }

            @Override
            public String setValue(String value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
