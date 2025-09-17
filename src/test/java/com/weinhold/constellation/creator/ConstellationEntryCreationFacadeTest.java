package com.weinhold.constellation.creator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weinhold.constellation.persistence.ConstellationDatabaseFacade;
import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

@ExtendWith(MockitoExtension.class)
class ConstellationEntryCreationFacadeTest {

    @Mock
    private ConstellationDatabaseFacade constellationDatabaseFacade;

    @InjectMocks
    private ConstellationEntryCreationFacade facade;

    @BeforeEach
    void setUp() {
        // nothing for now
    }

    @Test
    void scheduleGroups_peopleListIsEmpty_NoEntries() {
        var result = facade.scheduleGroups(new ArrayList<>(), 3, 5, UUID.randomUUID());

        assertEquals(0, result.size(), "No entries should be created for an empty people list");
        verify(constellationDatabaseFacade, never()).saveConstellationEntry(any());
    }

    @Test
    void scheduleGroups_sixPeopleWithGroupsOfThree_2GroupsWith3MembersFor2Rounds() {
        var people = mockPeople(6);
        var constellationId = UUID.randomUUID();

        var captor = ArgumentCaptor.forClass(ConstellationEntry.class);

        var result = facade.scheduleGroups(people, 3, 2, constellationId);

        verify(constellationDatabaseFacade, times(4)).saveConstellationEntry(captor.capture());
        assertEquals(4, result.size(), "Should persist 2 rounds × 2 groups = 4 entries");

        var ids = people.stream().map(p -> p.getId().toString()).collect(Collectors.toSet());
        assertEquals(6, ids.size());

        var saved = captor.getAllValues();
        var groupsPerRound = 2;

        for (int r = 0; r < 2; r++) {
            var roundSlice = saved.subList(r * groupsPerRound, (r + 1) * groupsPerRound);
            var roundIds = roundSlice.stream().flatMap(e -> Arrays.stream(e.getPersonIds().split(","))).toList();

            assertEquals(6, roundIds.size(), "Each round must place all participants exactly once");
            assertEquals(6, new HashSet<>(roundIds).size(), "No duplicate person assignments within a round");
            assertTrue(ids.containsAll(roundIds) && new HashSet<>(roundIds).containsAll(ids),
                "Round must include all original participant IDs");
        }

        for (ConstellationEntry entry : saved) {
            var size = entry.getPersonIds().isBlank() ? 0 : entry.getPersonIds().split(",").length;
            assertEquals(3, size, "Each group should have size 3 for 6 people with groupSize 3");
        }
    }

    @Test
    void scheduleGroups_tenPeopleWithGroupsOfThree_roundSizesAre3322() {
        var people = mockPeople(10);
        var constellationId = UUID.randomUUID();

        var captor = ArgumentCaptor.forClass(ConstellationEntry.class);

        var result = facade.scheduleGroups(people, 3, 1, constellationId);

        verify(constellationDatabaseFacade, times(4)).saveConstellationEntry(captor.capture());
        assertEquals(4, result.size(), "Should persist 1 round × 4 groups = 4 entries");

        var saved = captor.getAllValues();

        var allIds = people.stream().map(p -> p.getId().toString()).collect(Collectors.toSet());
        var roundIds = saved.stream().flatMap(e -> Arrays.stream(e.getPersonIds().split(","))).toList();
        assertEquals(10, roundIds.size(), "Total assigned must be 10 in the round");
        assertEquals(10, new HashSet<>(roundIds).size(), "No duplicates across groups in the round");
        assertTrue(allIds.containsAll(roundIds) && new HashSet<>(roundIds).containsAll(allIds),
            "Round must cover all participant IDs");

        var sizes =
            saved.stream().map(e -> e.getPersonIds().isBlank() ? 0 : e.getPersonIds().split(",").length).sorted().toList();
        assertEquals(List.of(2, 2, 3, 3), sizes, "Expected group size multiset [3,3,2,2]");
    }

    @Test
    void scheduleGroups_2PeopleButGroupSize3_returnsOneGroupOf2() {
        var people = mockPeople(2);
        var constellationId = UUID.randomUUID();

        var captor = ArgumentCaptor.forClass(ConstellationEntry.class);

        var result = facade.scheduleGroups(people, 3, 1, constellationId);

        verify(constellationDatabaseFacade, times(1)).saveConstellationEntry(captor.capture());
        assertEquals(1, result.size(), "Should persist a single group entry");

        var entry = captor.getValue();
        var ids = new HashSet<>(Arrays.asList(entry.getPersonIds().split(",")));
        var expected = people.stream().map(p -> p.getId().toString()).collect(Collectors.toSet());

        assertEquals(2, ids.size(), "Group should contain both participants");
        assertEquals(expected, ids, "Group membership must match the two input participants");
        assertEquals(1, entry.getRoundNumber(), "Single round index should be 1-based");
        assertEquals(1, entry.getGroupNumber(), "Single group index should be 1-based");
    }

    private static List<Person> mockPeople(int n) {
        var list = new ArrayList<Person>(n);
        for (int i = 0; i < n; i++) {
            var id = UUID.randomUUID();
            var p = mock(Person.class);
            when(p.getId()).thenReturn(id);
            list.add(p);
        }
        return list;
    }
}
