package com.weinhold.constellation.creator.files.workbook.plan;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.weinhold.constellation.creator.files.workbook.WorkbookSheetCreator;
import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

@Component
public class AnnualPlanSheetCreator implements WorkbookSheetCreator {

    private static final DateTimeFormatter DMY = ofPattern("dd.MM.yyyy", ENGLISH);
    private static final String SHEET_NAME = "Annual Plan";
    private static final String MONTHLY_HEADER = "Month Range";
    private static final String CALENDAR_WEEK_HEADER = "Calendar Week";
    private static final String DATE_HEADER = "Date";
    private static final String WEEK_RANGE_HEADER = "Week Range";
    private static final String CELL_DATE_FORMAT = "E, dd MMM yyyy";

    /**
     * Creates an "Annual Plan" sheet in the provided workbook based on the given constellation entries and year.
     * The method determines the cadence (monthly, weekly, daily) based on the number of entries and populates
     * the sheet with dates, round numbers, group numbers, and person IDs.
     *
     * @param workbook the Excel workbook where the sheet will be created
     * @param constellationEntries the list of constellation entries to be included in the plan
     * @param peopleOfConstellation the list of people involved in the constellation
     * @param dates the dates corresponding to the entries, must match the expected cadence
     * @throws IllegalArgumentException if the number of entries does not match expected cadences or if 366 entries are provided
     *         for a non-leap year
     */
    @Override
    public void createSheet(Workbook workbook, List<ConstellationEntry> constellationEntries, List<Person> peopleOfConstellation,
        List<LocalDate> dates) {
        constellationEntries.sort(
            comparingInt(ConstellationEntry::getRoundNumber).thenComparingInt(ConstellationEntry::getGroupNumber));

        var sheet = workbook.createSheet(SHEET_NAME);
        createHeader(sheet, dates);
        createGroupRows(sheet, constellationEntries, peopleOfConstellation);
        for (var c = 0; c <= dates.size(); c++) {
            sheet.autoSizeColumn(c);
        }
    }

    private void createHeader(Sheet sheet, List<LocalDate> dates) {
        switch (dates.size()) {
            case 12 -> createMonthlyHeader(sheet, dates);
            case 52, 53 -> createWeeklyHeader(sheet, dates);
            case 365, 366 -> createDailyHeader(sheet, dates);
            default -> throw new IllegalArgumentException("Unexpected number of entries: " + dates.size());
        }
    }

    private void createMonthlyHeader(Sheet sheet, List<LocalDate> dates) {
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue(MONTHLY_HEADER);
        for (var i = 0; i < 12; i++) {
            var start = dates.get(i);
            var end = start.with(TemporalAdjusters.lastDayOfMonth());
            header.createCell(i + 1).setCellValue(formatRange(start, end));
        }
    }

    private void createWeeklyHeader(Sheet sheet, List<LocalDate> dates) {
        var headerCW = sheet.createRow(0);
        var headerRange = sheet.createRow(1);
        headerCW.createCell(0).setCellValue(CALENDAR_WEEK_HEADER);
        headerRange.createCell(0).setCellValue(WEEK_RANGE_HEADER);

        for (var i = 0; i < dates.size(); i++) {
            var start = dates.get(i); // Monday
            var end = start.plusDays(6);
            var cw = start.get(WeekFields.ISO.weekOfWeekBasedYear());

            headerCW.createCell(i + 1).setCellValue(cw);
            headerRange.createCell(i + 1).setCellValue(formatRange(start, end));
        }
    }

    private static void createDailyHeader(Sheet sheet, List<LocalDate> dates) {
        var headerCalendarWeek = sheet.createRow(0);
        var headerDates = sheet.createRow(1);
        headerCalendarWeek.createCell(0).setCellValue(CALENDAR_WEEK_HEADER);
        headerDates.createCell(0).setCellValue(DATE_HEADER);

        var currentCW = -1;
        for (var i = 0; i < dates.size(); i++) {
            var d = dates.get(i);
            var cw = d.get(WeekFields.ISO.weekOfWeekBasedYear());
            if (cw != currentCW) {
                currentCW = cw;
                headerCalendarWeek.createCell(i + 1).setCellValue(currentCW);
            }
            headerDates.createCell(i + 1).setCellValue(d.format(ofPattern(CELL_DATE_FORMAT, ENGLISH)));
        }
    }

    private String formatRange(LocalDate start, LocalDate end) {
        return start.format(DMY) + " - " + end.format(DMY);
    }

    private void createGroupRows(Sheet sheet, List<ConstellationEntry> constellationEntries, List<Person> peopleOfConstellation) {
        var personNameById = peopleOfConstellation.stream().collect(toMap(Person::getId, Person::getName));
        var constellationsByRound = constellationEntries.stream().collect(groupingBy(ConstellationEntry::getRoundNumber));

        var rowNumber = 3;
        for (var i = 0; i < constellationsByRound.size(); i++) {
            var constellations = constellationsByRound.get(i + 1);
            for (var constellation : constellations) {
                var personIds = stream(constellation.getPersonIds().split(",")).map(UUID::fromString).toList();
                for (var id : personIds) {
                    var currentRow = sheet.getRow(rowNumber);
                    var row = currentRow == null ? sheet.createRow(rowNumber) : currentRow;
                    row.createCell(i + 1).setCellValue(personNameById.get(id));
                    rowNumber++;
                }
                rowNumber++;
            }
            rowNumber = 3;
        }
    }
}
