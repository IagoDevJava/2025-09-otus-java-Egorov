package ru.otus.homework;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Customer {

    private final long id;

    @Setter
    private String name;

    @Setter
    private long scores;

    public Customer(long id, String name, long scores) {
        this.id = id;
        this.name = name;
        this.scores = scores;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", name='" + name + '\'' + ", scores=" + scores + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Customer customer = (Customer) o;
        return id == customer.id; // ← ТОЛЬКО по id!
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // ← ТОЛЬКО от id!
    }
}
