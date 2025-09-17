package com.weinhold.constellation.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weinhold.constellation.persistence.model.Person;

/**
 * Repository interface for managing Person entities.
 */
public interface PersonRepository extends JpaRepository<Person, UUID> {

    /**
     * Finds all persons associated with a specific run.
     *
     * @param constellationId the unique identifier for the constellation
     * @return a list of persons associated with the given constellationId
     */
    List<Person> findByConstellationId(UUID constellationId);
}
