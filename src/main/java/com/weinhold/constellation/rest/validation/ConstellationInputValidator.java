package com.weinhold.constellation.rest.validation;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.time.Year;

import com.weinhold.constellation.rest.model.ConstellationInput;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ConstellationInputValidator {

    /**
     * Validates the given ConstellationInput.
     *
     * @param input the ConstellationInput to validate
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public static void validateInput(ConstellationInput input) {
        if (input.getPeople() == null || input.getPeople().size() < 4) {
            throw new IllegalArgumentException("At least 4 people are required");
        }
        if (input.getNumberOfGroups() < 2) {
            throw new IllegalArgumentException("There must be at least 2 groups");
        }
        if (input.getRotation() == null) {
            throw new IllegalArgumentException("Rotation must be specified");
        }
        var currentYear = Year.now().getValue();
        if (input.getYear() < currentYear) {
            throw new IllegalArgumentException(format("Year must be %s or later", currentYear));
        }
    }
}
