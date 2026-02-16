package ru.otus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "address")
public class Address {

  @Id
  @SequenceGenerator(name = "address_gen", sequenceName = "address_seq", initialValue = 1, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_gen")
  @Column(name = "id")
  private Long id;

  @Column(name = "street")
  private String street;

  public Address(Long id, String street) {
    this.id = id;
    this.street = street;
  }

  public Address(String street) {
    this.street = street;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Address)) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(id, address.id) && Objects.equals(street, address.street);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, street);
  }

  @Override
  public String toString() {
    return "Address{" + "id=" + id + ", street='" + street + '\'' + '}';
  }
}
