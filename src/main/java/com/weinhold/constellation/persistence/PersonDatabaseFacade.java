package com.weinhold.constellation.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.weinhold.constellation.persistence.model.Person;
import com.weinhold.constellation.persistence.repository.PersonRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class PersonDatabaseFacade {

    private final PersonRepository personRepository;

    /**
     * Saves a person with the given name to the database.
     *
     * @param constellationId the unique identifier for the current constellation
     * @param personName the name of the person to be saved
     */
    public Person savePerson(UUID constellationId, String personName) {
        var person = new Person();
        person.setId(UUID.randomUUID());
        person.setConstellationId(constellationId);
        person.setName(personName);
        personRepository.save(person);
        log.debug("Saved person: {} for run: {}", person, constellationId);
        return person;
    }

    /**
     * Finds a person by their unique identifier.
     *
     * @param id the UUID of the person to be found
     * @return an Optional containing the found Person or empty if not found
     */
    public Optional<Person> findPersonById(UUID id) {
        log.debug("Finding person with ID: {}", id);
        return personRepository.findById(id);
    }

    /**
     * Finds all persons associated with a specific run.
     *
     * @param constellationId the unique identifier for the run
     * @return a list of persons associated with the given constellationId
     */
    public List<Person> findAllPeopleOfConstellation(UUID constellationId) {
        log.debug("Finding all people of constellation: {}", constellationId);
        return personRepository.findByConstellationId(constellationId);
    }

    /**
     * Deletes a person by their unique identifier.
     *
     * @param constellationId the UUID of the person to be deleted
     */
    public void deleteAllPeopleOfConstellation(UUID constellationId) {
        log.debug("Deleting all people for constellation: {}", constellationId);
        personRepository.deleteAll(personRepository.findByConstellationId(constellationId));
    }
}
