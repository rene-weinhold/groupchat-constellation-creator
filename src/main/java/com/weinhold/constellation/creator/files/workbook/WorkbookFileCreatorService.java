package com.weinhold.constellation.creator.files.workbook;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.weinhold.constellation.creator.files.FileCreator;
import com.weinhold.constellation.creator.files.model.FileCreationInput;
import com.weinhold.constellation.creator.files.workbook.plan.AnnualPlanSheetCreator;
import com.weinhold.constellation.creator.files.workbook.statistics.StatisticsSheetCreator;
import com.weinhold.constellation.persistence.PersonDatabaseFacade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class WorkbookFileCreatorService implements FileCreator {

    private final PersonDatabaseFacade personDatabaseFacade;
    private final AnnualPlanSheetCreator annualPlanSheetCreator;
    private final StatisticsSheetCreator statisticsSheetCreator;

    /**
     * Generates an Excel workbook containing the annual plan and the statistics based on the provided constellation entries.
     *
     * @param input the input containing constellation ID, entries, dates, and people
     */
    @Override
    public File createFile(FileCreationInput input) {
        var fileName = format("Annual Plan - %s.xlsx", input.getConstellationId());
        var file = new File(fileName);
        try (var workbook = new XSSFWorkbook(); var out = new FileOutputStream(file)) {
            var peopleOfRun = personDatabaseFacade.findAllPeopleOfConstellation(input.getConstellationId());

            annualPlanSheetCreator.createSheet(workbook, input.getEntries(), peopleOfRun, input.getDates());
            statisticsSheetCreator.createSheet(workbook, input.getEntries(), peopleOfRun, input.getDates());

            workbook.write(out);
            return file;
        } catch (IOException e) {
            log.error("Error generating workbook for constellationId {}: {}", input.getConstellationId(), e.getMessage(), e);
        }
        return null;
    }

}
