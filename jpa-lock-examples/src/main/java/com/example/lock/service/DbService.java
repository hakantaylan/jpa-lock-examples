package com.example.lock.service;

import com.example.lock.entity.Flight;
import com.example.lock.entity.Ticket;
import com.example.lock.exception.ExceededCapacityException;
import com.example.lock.repository.FlightRepository;
import com.example.lock.repository.TicketRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class DbService {

    private final FlightRepository flightRepository;

    private final TicketRepository ticketRepository;

    public DbService(FlightRepository flightRepository, TicketRepository ticketRepository) {
        this.flightRepository = flightRepository;
        this.ticketRepository = ticketRepository;
    }

    private void saveNewTicket(String firstName, String lastName, Flight flight) throws Exception {
        if (flight.getCapacity() <= flight.getTickets().size()) {
            throw new ExceededCapacityException();
        }
        var ticket = new Ticket();
        ticket.setFirstName(firstName);
        ticket.setLastName(lastName);
        flight.addTicket(ticket);
        ticketRepository.save(ticket);
    }

    private void fetchAndChangeFlight(long flightId) throws Exception {
        var flight = flightRepository.findWithLockingById(flightId).get();
        flight.setCapacity(flight.getCapacity() + 1);
        Thread.sleep(1_000);
    }

    @Transactional
    public void changeFlight1() throws Exception {
//        var flight = flightRepository.findById(1L).get();
        var flight = flightRepository.findWithLockingById(1L).get();
        saveNewTicket("Robert", "Smith", flight);
//        flight.setCapacity(10);
//        fetchAndChangeFlight(1L);
        Thread.sleep(1_000);
    }

    @Transactional
    public void changeFlight2() throws Exception {
//        var flight = flightRepository.findById(1L).get();
        var flight = flightRepository.findWithLockingById(1L).get();
        saveNewTicket("Kate", "Brown", flight);
//        flight.setCapacity(20);
//        fetchAndChangeFlight(1L);
        Thread.sleep(1_000);
    }

}
