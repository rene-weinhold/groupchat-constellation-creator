package com.weinhold.constellation.rest;

import static com.weinhold.constellation.rest.validation.ConstellationInputValidator.validateInput;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.weinhold.constellation.creator.ConstellationCreationFacade;
import com.weinhold.constellation.persistence.ConstellationDatabaseFacade;
import com.weinhold.constellation.persistence.PersonDatabaseFacade;
import com.weinhold.constellation.rest.model.ConstellationInput;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
public class ConstellationController {

    private static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"";

    private final PersonDatabaseFacade personDatabaseFacade;
    private final ConstellationDatabaseFacade constellationDatabaseFacade;
    private final ConstellationCreationFacade constellationCreationFacade;

    /**
     * Endpoint to create constellations based on the provided input.
     *
     * @param input the constellation input containing people, number of groups, rotation, and year
     * @return a schedule of groups for each round
     */
    @PostMapping(value = "/api/v1/constellations", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Object> createConstellations(@RequestBody ConstellationInput input) throws IOException {
        var constellationId = UUID.randomUUID();
        log.info("Creating constellation with id {}: {}", constellationId, input);
        try {
            validateInput(input);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for constellation creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        var constellationFile = constellationCreationFacade.createConstellationFile(input, constellationId);
        return ResponseEntity.ok() // Should be created (201), but I did not implement Upload with Url, so ok (200) will do
                             .header(CONTENT_DISPOSITION, format(CONTENT_DISPOSITION_FORMAT, constellationFile.getName()))
                             .contentType(parseMediaType(APPLICATION_OCTET_STREAM_VALUE))
                             .body(readAllBytes(constellationFile.toPath()));
    }

    @GetMapping(value = "/api/v1/constellations/{constellationId}", produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getConstellation(@PathVariable String constellationId) {
        log.info("Received request to find constellation with id {}", constellationId);

        // TODO: Get all entries from DB and generate workbook on the fly or download from storage
        return ResponseEntity.ok().contentType(parseMediaType(APPLICATION_OCTET_STREAM_VALUE)).body(new byte[0]);
    }

    @DeleteMapping(value = "/api/v1/constellations/{constellationId}")
    public ResponseEntity<Void> deleteConstellation(@PathVariable String constellationId) {
        log.info("Received request to delete for constellation with id {}", constellationId);
        var constellationUUID = UUID.fromString(constellationId);
        personDatabaseFacade.deleteAllPeopleOfConstellation(constellationUUID);
        constellationDatabaseFacade.deleteAllEntriesOfConstellation(constellationUUID);
        // TODO: Delete constellation from storage
        return ResponseEntity.noContent().build();
    }

}
