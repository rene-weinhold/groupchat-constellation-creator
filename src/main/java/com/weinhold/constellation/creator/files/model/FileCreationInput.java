package com.weinhold.constellation.creator.files.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileCreationInput {

    private UUID constellationId;
    private List<ConstellationEntry> entries;
    private List<LocalDate> dates;
    private List<Person> people;
    private int year;
}
