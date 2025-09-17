package com.weinhold.constellation.persistence.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "people")
public class Person {

    @Id
    @Column
    private UUID id;
    @Column
    private UUID constellationId;
    @Column
    private String name;
}
