package com.weinhold.constellation.persistence.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "constellation_entries")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConstellationEntry {

    @Id
    private UUID id;
    @Column
    private UUID constellationId;
    @Column
    private int roundNumber;
    @Column
    private int groupNumber;
    @Column
    private String personIds;

}
