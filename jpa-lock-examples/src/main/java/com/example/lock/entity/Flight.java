package com.example.lock.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "flights")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private LocalDateTime departureTime;

    private Integer capacity;

    @OneToMany(mappedBy = "flight")
    @ToString.Exclude
    private Set<Ticket> tickets;

    @Version
    private Long version;


    public void addTicket(Ticket ticket) {
        ticket.setFlight(this);
        getTickets().add(ticket);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id) && Objects.equals(number, flight.number) && Objects.equals(departureTime, flight.departureTime) && Objects.equals(capacity, flight.capacity) && Objects.equals(tickets, flight.tickets) && Objects.equals(version, flight.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, departureTime, capacity, tickets, version);
    }
}