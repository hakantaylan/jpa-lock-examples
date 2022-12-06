package com.example.lox.repository;

import com.example.lox.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationRepository {

    private final JdbcTemplate template;

    public Reservation findById(int id) {
        Reservation reservation = this.template.queryForObject("select * from reservation where id =? ", Reservation.class, id);
        return reservation;
    }

    public List<Reservation> findAll() {
        return this.template.queryForList("select * from reservation ", Reservation.class);
    }

    public Reservation save(Reservation r) {
        return this
                .template
                .execute("update reservation set name = ? where id =? ", (PreparedStatement ps) -> {
                    ps.setString(1, r.getName());
                    ps.setInt(2, r.getId());
                    ps.execute();
                    return findById(r.getId());
                });
    }
}
