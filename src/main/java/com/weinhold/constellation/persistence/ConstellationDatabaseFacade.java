package com.weinhold.constellation.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.repository.ConstellationRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class ConstellationDatabaseFacade {

    private final ConstellationRepository constellationRepository;

    /**
     * Saves a constellation entry to the database.
     *
     * @param entry the constellation entry to be saved
     */
    public void saveConstellationEntry(ConstellationEntry entry) {
        log.debug("Saving constellation entry: {}", entry);
        constellationRepository.save(entry);
    }

    /**
     * Finds all constellation entries associated with a specific constellation ID.
     *
     * @param constellationId the UUID of the constellation
     * @return a list of constellation entries for the given constellation ID
     */
    public List<ConstellationEntry> findAllEntriesOfConstellation(UUID constellationId) {
        log.debug("Finding all constellation entries for run ID: {}", constellationId);
        return constellationRepository.findByConstellationId(constellationId);
    }

    /**
     * Deletes all constellation entries associated with a specific constellation ID.
     *
     * @param constellationId the UUID of the constellation
     */
    public void deleteAllEntriesOfConstellation(UUID constellationId) {
        log.debug("Deleting all constellation entries of constellation {}", constellationId);
        constellationRepository.deleteAll(constellationRepository.findByConstellationId(constellationId));
    }
}
