package com.weinhold.constellation.creator.files.workbook;

import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

/**
 * Marker interface for workbook sheet creators.
 */
public interface WorkbookSheetCreator {

    void createSheet(Workbook workbook, List<ConstellationEntry> constellationEntries, List<Person> peopleOfConstellation,
        List<LocalDate> dates);
}
