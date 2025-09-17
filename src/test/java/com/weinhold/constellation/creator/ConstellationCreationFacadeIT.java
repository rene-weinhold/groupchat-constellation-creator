package com.weinhold.constellation.creator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.weinhold.constellation.creator.files.zip.ZipFileCreatorService;
import com.weinhold.constellation.persistence.PersonDatabaseFacade;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class ConstellationCreationFacadeIT {

    @Mock
    private PersonDatabaseFacade personDatabaseFacade;
    @Mock
    private ConstellationEntryCreationFacade constellationEntryCreationFacade;
    @Mock
    private DateCreationFacade dateCreationFacade;
    @Mock
    private ZipFileCreatorService zipFileCreatorService;

    @InjectMocks
    private ConstellationCreationFacade constellationCreationFacade;

    @Test
    void createConstellationFile_withValidInputAndUUID_expectZipFileContaining2Files() {
        // Check for xlsx file
        // -> Check if workbook contains 2 sheets
        // -> Check if sheets have correct names
        // -> Check is sheets contain correct data: e.G: number of columns and rows
        // Check for ics file
        // -> Check if file contains correct data
    }

    @Test
    void createConstellationFile_withInvalidInputAndUUID_expectErrors() {
        // Check if zip is returned
        // -> Check if zip contains files or not
        // -> Check what files are contained, if there should be some
        // -> Check on what is saved to database
        // -> Check on what is saved to storage
        // -> If Saved to DB/Storage, will they be removed (if desired)
    }
}
