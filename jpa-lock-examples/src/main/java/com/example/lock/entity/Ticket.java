package com.example.lock.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @ToString.Exclude
    private Flight flight;

    private String firstName;

    private String lastName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id) && Objects.equals(flight.getId(), ticket.flight.getId()) && Objects.equals(firstName, ticket.firstName) && Objects.equals(lastName, ticket.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flight.getId(), firstName, lastName);
    }
}