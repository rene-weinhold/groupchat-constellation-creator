package com.weinhold.constellation.creator;

import java.io.File;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.weinhold.constellation.creator.files.model.FileCreationInput;
import com.weinhold.constellation.creator.files.zip.ZipFileCreatorService;
import com.weinhold.constellation.persistence.PersonDatabaseFacade;
import com.weinhold.constellation.rest.model.ConstellationInput;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class ConstellationCreationFacade {

    private final PersonDatabaseFacade personDatabaseFacade;
    private final ConstellationEntryCreationFacade constellationEntryCreationFacade;
    private final DateCreationFacade dateCreationFacade;
    private final ZipFileCreatorService zipFileCreatorService;

    /**
     * Creates a constellation file based on the provided input and constellation ID.
     *
     * @param input the input containing people, number of groups, year, and rotation
     * @param constellationId the unique identifier for the constellation
     * @return the created constellation file
     */
    public File createConstellationFile(ConstellationInput input, UUID constellationId) {
        var groupSize = Math.max(2, input.getPeople().size() / Math.max(1, input.getNumberOfGroups()));

        var people =
            input.getPeople().stream().map(personName -> personDatabaseFacade.savePerson(constellationId, personName)).toList();

        var dates = dateCreationFacade.buildDatesForYear(input.getYear(), input.getRotation());

        var schedule = constellationEntryCreationFacade.scheduleGroups(people, groupSize, dates.size(), constellationId);
        var fileCreationInput = FileCreationInput.builder()
                                                 .constellationId(constellationId)
                                                 .year(input.getYear())
                                                 .people(people)
                                                 .dates(dates)
                                                 .entries(schedule)
                                                 .build();
        return zipFileCreatorService.createFile(fileCreationInput);
    }
}
