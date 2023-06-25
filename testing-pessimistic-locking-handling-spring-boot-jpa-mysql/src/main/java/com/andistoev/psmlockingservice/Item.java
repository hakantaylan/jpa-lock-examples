package com.andistoev.psmlockingservice;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Data
@Entity
public class Item {

    @Id
    @Column(length = 16)
    private UUID id = UUID.randomUUID();

    private int amount = 0;
}
