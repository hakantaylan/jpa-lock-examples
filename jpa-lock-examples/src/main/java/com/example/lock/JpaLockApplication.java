package com.example.lock;

import com.example.lock.entity.Flight;
import com.example.lock.entity.Ticket;
import com.example.lock.repository.FlightRepository;
import com.example.lock.repository.TicketRepository;
import com.example.lock.service.DbService;
import org.apache.commons.lang3.function.FailableRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class JpaLockApplication implements CommandLineRunner {

    @Resource
    private DbService dbService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public static void main(String[] args) {
        SpringApplication.run(JpaLockApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            Flight flight = Flight.builder().id(1L).capacity(2).departureTime(LocalDateTime.now()).number("FLT123").build();
            flightRepository.save(flight);
            Ticket ticket = new Ticket();
            ticket.setFlight(flight);
            ticket.setFirstName("Paul");
            ticket.setLastName("Lee");
            ticketRepository.save(ticket);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(safeRunnable(dbService::changeFlight1));
            executor.execute(safeRunnable(dbService::changeFlight2));
            boolean b = executor.awaitTermination(5, TimeUnit.SECONDS);
            executor.shutdown();
//            ticketRepository.findAll();
            flightRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable safeRunnable(FailableRunnable<Exception> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}