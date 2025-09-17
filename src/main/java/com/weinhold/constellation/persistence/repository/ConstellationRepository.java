package com.weinhold.constellation.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weinhold.constellation.persistence.model.ConstellationEntry;

/**
 * Repository interface for managing Person entities.
 */
public interface ConstellationRepository extends JpaRepository<ConstellationEntry, UUID> {

    /**
     * Finds all constellation entries associated with a specific run ID.
     *
     * @param constellationId the UUID of the constellation
     * @return a list of constellation entries for the given constellation ID
     */
    List<ConstellationEntry> findByConstellationId(UUID constellationId);
}
