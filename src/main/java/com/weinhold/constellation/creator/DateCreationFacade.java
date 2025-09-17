package com.weinhold.constellation.creator;

import static java.time.DayOfWeek.THURSDAY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.weinhold.constellation.rest.model.Rotation;

@Component
public class DateCreationFacade {

    /**
     * Builds a list of dates for the specified year based on the given rotation (cadence).
     * The method supports three types of rotations: MONTHLY, WEEKLY, and DAILY.
     *
     * @param year the year for which to generate the dates
     * @param rotation the rotation type (MONTHLY, WEEKLY, DAILY)
     * @return a list of LocalDate objects representing the dates for the specified year and rotation
     */
    public List<LocalDate> buildDatesForYear(int year, Rotation rotation) {
        return switch (rotation) {
            case MONTHLY -> buildMonthlyDates(year);
            case WEEKLY -> buildWeeklyDates(year);
            case DAILY -> buildDailyDates(year);
        };
    }

    private List<LocalDate> buildMonthlyDates(int year) {
        var list = new ArrayList<LocalDate>();
        for (var m = 1; m <= 12; m++) {
            list.add(LocalDate.of(year, m, 1));
        }
        return list;
    }

    private List<LocalDate> buildWeeklyDates(int year) {
        var iso = WeekFields.ISO;

        // ISO week 1 is the week that contains the first Thursday of January
        var firstThursday = LocalDate.of(year, 1, 1).with(TemporalAdjusters.nextOrSame(THURSDAY));

        var firstMonday = firstThursday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var list = new ArrayList<LocalDate>(53);
        for (var d = firstMonday; d.get(iso.weekBasedYear()) == year; d = d.plusWeeks(1)) {
            list.add(d);
        }
        return list;
    }

    private List<LocalDate> buildDailyDates(int year) {
        var start = LocalDate.of(year, 1, 1);
        var list = new ArrayList<LocalDate>();
        for (var i = 0; i < (Year.isLeap(year) ? 366 : 365); i++) {
            list.add(start.plusDays(i));
        }
        return list;
    }
}
