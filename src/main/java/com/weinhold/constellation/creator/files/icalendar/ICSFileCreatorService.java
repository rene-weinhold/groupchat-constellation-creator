package com.weinhold.constellation.creator.files.icalendar;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.weinhold.constellation.creator.files.FileCreator;
import com.weinhold.constellation.creator.files.model.FileCreationInput;
import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Method;
import biweekly.property.ProductId;
import biweekly.util.ICalDate;

@Service
public class ICSFileCreatorService implements FileCreator {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final String PRODUCT_ID = "-//Weinhold//Groupchat//EN";
    private static final String FILE_NAME_PATTERN = "Constellations-%s.ics";
    private static final String EVENT_NAME_PATTERN = "Group chat – Group %s (%s )";

    /**
     * Generates an ICS file containing events for each constellation entry based on the provided input.
     * The events are created according to the cadence determined by the maximum round number (monthly, weekly, daily).
     *
     * @param input the input containing constellation ID, entries, people, and year
     * @return the created ICS file
     * @throws IllegalArgumentException if the cadence is unsupported
     */
    @Override
    public File createFile(FileCreationInput input) {

        var fileName = format(FILE_NAME_PATTERN, input.getConstellationId());
        var file = new File(fileName);

        var maxRound = input.getEntries().stream().mapToInt(ConstellationEntry::getRoundNumber).max().orElse(0);
        var ranges = getRanges(input, maxRound);
        var nameById = input.getPeople().stream().collect(Collectors.toMap(Person::getId, Person::getName));

        var iCal = new ICalendar();
        iCal.setProductId(new ProductId(PRODUCT_ID));
        iCal.setMethod(Method.publish());
        input.getEntries().stream().map(e -> createEvent(e, ranges, nameById)).forEach(iCal::addEvent);

        try {
            Biweekly.write(iCal).go(file);
        } catch (Exception ex) {
            throw new RuntimeException("Error writing ICS file", ex);
        }
        return file;
    }

    private List<LocalDate[]> getRanges(FileCreationInput input, int maxRound) {
        return switch (maxRound) {
            case 12 -> monthlyRanges(input.getYear());
            case 52, 53 -> weeklyRangesIso(input.getYear());
            case 365, 366 -> dailyRanges(input.getYear());
            default -> throw new IllegalStateException("Unexpected cadence");
        };
    }

    private List<LocalDate[]> monthlyRanges(int year) {
        var out = new ArrayList<LocalDate[]>();
        for (int m = 1; m <= 12; m++) {
            var start = LocalDate.of(year, m, 1);
            var endExclusive = (m < 12) ? LocalDate.of(year, m + 1, 1) : LocalDate.of(year + 1, 1, 1);
            out.add(new LocalDate[] { start, endExclusive });
        }
        return out;
    }

    private List<LocalDate[]> weeklyRangesIso(int year) {
        var weeks = new ArrayList<LocalDate[]>();

        var firstThursday = LocalDate.of(year, 1, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
        var monday = firstThursday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (var d = monday; d.get(WeekFields.ISO.weekBasedYear()) == year; d = d.plusWeeks(1)) {
            var endExclusive = d.plusDays(7);
            weeks.add(new LocalDate[] { d, endExclusive });
        }
        return weeks;
    }

    private List<LocalDate[]> dailyRanges(int year) {
        var start = LocalDate.of(year, 1, 1);
        var daysInYear = Year.isLeap(year) ? 366 : 365;
        var out = new ArrayList<LocalDate[]>(daysInYear);
        for (int i = 0; i < daysInYear; i++) {
            var s = start.plusDays(i);
            out.add(new LocalDate[] { s, s.plusDays(1) });
        }
        return out;
    }

    private VEvent createEvent(ConstellationEntry entry, List<LocalDate[]> ranges, Map<UUID, String> nameById) {
        var roundIdx = entry.getRoundNumber();

        var range = ranges.get(roundIdx - 1);
        var start = range[0];
        var endExclusive = range[1];

        var names =
            parseUuids(entry.getPersonIds()).stream().map(id -> ofNullable(nameById.get(id)).orElseGet(id::toString)).toList();

        var title = format(EVENT_NAME_PATTERN, entry.getGroupNumber(), String.join(", ", names));

        var vevent = new VEvent();
        vevent.setDateStart(iCalAllDay(start));
        vevent.setDateEnd(iCalAllDay(endExclusive));
        vevent.setSummary(title);
        vevent.setUid(UUID.randomUUID().toString());
        return vevent;
    }

    private List<UUID> parseUuids(String csv) {
        return (csv == null || csv.isBlank()) ? List.of()
            : stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(UUID::fromString).toList();
    }

    private ICalDate iCalAllDay(LocalDate d) {
        return new ICalDate(Date.from(d.atStartOfDay(ZONE).toInstant()), false);
    }
}
