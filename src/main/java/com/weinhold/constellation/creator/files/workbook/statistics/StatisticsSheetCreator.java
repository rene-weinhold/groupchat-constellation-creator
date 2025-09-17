package com.weinhold.constellation.creator.files.workbook.statistics;

import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.weinhold.constellation.creator.files.workbook.WorkbookSheetCreator;
import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class StatisticsSheetCreator implements WorkbookSheetCreator {

    private static final String SHEET_NAME = "Statistics";
    private static final String PERSON_HEADER = "Person";

    /**
     * Creates a "Statistics" sheet in the provided workbook based on the given constellation entries and people of the run.
     * The method populates the sheet with a matrix showing how many times each pair of people have been grouped together.
     *
     * @param workbook the Excel workbook where the sheet will be created
     * @param constellationEntries the list of constellation entries to be analyzed
     * @param peopleOfConstellation the list of people involved in the constellation
     * @param dates the dates corresponding to the entries (not used in this method)
     */
    @Override
    public void createSheet(Workbook workbook, List<ConstellationEntry> constellationEntries, List<Person> peopleOfConstellation,
        List<LocalDate> dates) {
        var sheet = workbook.createSheet(SHEET_NAME);
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue(PERSON_HEADER);
        var cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        for (var index = 0; index < peopleOfConstellation.size(); index++) {
            var cell = header.createCell(index + 1);
            cell.setCellValue(peopleOfConstellation.get(index).getName());
        }
        for (var rowIndex = 0; rowIndex < peopleOfConstellation.size(); rowIndex++) {
            var row = sheet.createRow(rowIndex + 1);
            var personA = peopleOfConstellation.get(rowIndex);
            row.createCell(0).setCellValue(personA.getName());
            for (var columnIndex = 0; columnIndex < peopleOfConstellation.size(); columnIndex++) {
                var personB = peopleOfConstellation.get(columnIndex);
                var cell = row.createCell(columnIndex + 1);
                if (personA.equals(personB)) {
                    cell.setCellValue("-");
                } else {
                    cell.setCellValue(
                        constellationEntries.stream()
                                            .filter(entry -> entry.getPersonIds().contains(personA.getId().toString())
                                                && entry.getPersonIds().contains(personB.getId().toString()))
                                            .count());
                }
                cell.setCellStyle(cellStyle);
            }
        }
    }
}
