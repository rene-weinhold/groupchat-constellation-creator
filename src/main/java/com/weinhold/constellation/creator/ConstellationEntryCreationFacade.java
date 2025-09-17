package com.weinhold.constellation.creator;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.joining;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.weinhold.constellation.persistence.ConstellationDatabaseFacade;
import com.weinhold.constellation.persistence.model.ConstellationEntry;
import com.weinhold.constellation.persistence.model.Person;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class ConstellationEntryCreationFacade {

    // ---- Tunable penalties ----
    private static final int PENALTY_HISTORY = 10; // weight for historic pair count
    private static final int PENALTY_RECENT = 1000; // big penalty if pair met in recent window
    private static final int PENALTY_SIZE_SKEW = 2; // gentle push to fill groups evenly

    private static final int window = 3; // "no-repeat" window (pairs met in the last `window` rounds are penalized heavily)
    private static final int restarts = 200; // randomized restarts to hunt for a better schedule (e.g., 200)

    private final ConstellationDatabaseFacade constellationDatabaseFacade;

    /**
     * Generate group schedules that maximize variation (fewest repeated pairings).
     *
     * @param people list of unique participant IDs
     * @param groupSize target group size (will balance to ±1 if not divisible)
     * @param rounds number of rounds to schedule
     * @param constellationId unique identifier for this scheduling constellation
     * @return all constellation entries created for this run
     */
    public List<ConstellationEntry> scheduleGroups(List<Person> people, int groupSize, int rounds, UUID constellationId) {
        var rng = new Random();
        var targetSizes = computeTargetSizes(people.size(), groupSize);

        List<List<List<UUID>>> bestSchedule = null;
        var bestScore = Long.MAX_VALUE;

        for (var r = 0; r < max(1, restarts); r++) {
            var result = tryOneRestart(people, targetSizes, rounds, rng);
            if (result == null) {
                continue;
            }
            if (result.finalScore < bestScore) {
                bestScore = result.finalScore;
                bestSchedule = result.schedule;
            }
        }
        if (bestSchedule == null) {
            throw new IllegalStateException("Failed to build a schedule");
        }
        return persistSchedule(bestSchedule, constellationId);
    }

    private List<Integer> computeTargetSizes(int n, int groupSize) {
        var groups = (int) Math.ceil(n / (double) groupSize);
        var base = n / groups;
        var extra = n % groups;

        var sizes = new ArrayList<Integer>(groups);
        for (var i = 0; i < groups; i++) {
            sizes.add(base + (i < extra ? 1 : 0));
        }
        return sizes;
    }

    private Result tryOneRestart(List<Person> people, List<Integer> targetSizes, int rounds, Random rng) {
        var n = people.size();
        var indexById = indexById(people);
        var pairCnt = new int[n][n];
        var recent = new ArrayDeque<Set<Long>>(max(1, window));
        var schedule = new ArrayList<List<List<UUID>>>(rounds);
        var totalScore = 0L;

        for (var r = 0; r < rounds; r++) {
            var round = buildRound(people, targetSizes, indexById, pairCnt, recent, rng);
            if (!round.success) {
                return null;
            }
            totalScore += round.deltaScore;
            updateHistory(round.groups, indexById, pairCnt, recent);
            schedule.add(round.groups);
        }

        var imbalance = pairImbalance(pairCnt);
        var finalScore = totalScore * 1000 + imbalance; // base score dominates, imbalance tie-breaks
        return new Result(schedule, finalScore);
    }

    private Map<UUID, Integer> indexById(List<Person> people) {
        var map = new HashMap<UUID, Integer>(people.size() * 2);
        for (var i = 0; i < people.size(); i++) {
            map.put(people.get(i).getId(), i);
        }
        return map;
    }

    private Round buildRound(List<Person> people, List<Integer> targetSizes, Map<UUID, Integer> indexById, int[][] pairCnt,
        Deque<Set<Long>> recent, Random rng) {
        var groups = targetSizes.size();

        var pool = new ArrayList<>(people);
        Collections.shuffle(pool, rng);

        var roundGroups = new ArrayList<List<UUID>>();
        for (var g = 0; g < groups; g++) {
            roundGroups.add(new ArrayList<>());
        }
        var thisRoundPairs = new HashSet<Long>();
        var deltaSum = 0L;

        while (!pool.isEmpty()) {
            var p = pool.removeLast();
            var pi = indexById.get(p.getId());

            var choice = chooseBestGroup(pi, roundGroups, targetSizes, indexById, pairCnt, recent);
            if (choice.groupIndex < 0) {
                return Round.fail(); // no feasible slot
            }
            roundGroups.get(choice.groupIndex).add(p.getId());
            deltaSum += choice.deltaScore;
        }

        return Round.success(roundGroups, thisRoundPairs, deltaSum);
    }

    private Choice chooseBestGroup(int pi, List<List<UUID>> roundGroups, List<Integer> targetSizes, Map<UUID, Integer> indexById,
        int[][] pairCnt, Deque<Set<Long>> recent) {
        var bestDelta = Long.MAX_VALUE;
        var bestG = -1;

        for (var g = 0; g < roundGroups.size(); g++) {
            if (roundGroups.get(g).size() >= targetSizes.get(g)) {
                continue;
            }
            var delta = 0L;
            for (var mate : roundGroups.get(g)) {
                var qi = indexById.get(mate);
                delta += (long) PENALTY_HISTORY * pairCnt[min(pi, qi)][max(pi, qi)];
                if (metRecently(pi, qi, recent)) {
                    delta += PENALTY_RECENT;
                }
            }
            delta += (long) PENALTY_SIZE_SKEW * roundGroups.get(g).size();

            if (delta < bestDelta) {
                bestDelta = delta;
                bestG = g;
            }
        }
        return new Choice(bestG, bestDelta);
    }

    private boolean metRecently(int a, int b, Deque<Set<Long>> recent) {
        var key = pairKey(a, b);
        for (var past : recent) {
            if (past.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private void updateHistory(List<List<UUID>> groups, Map<UUID, Integer> indexById, int[][] pairCnt, Deque<Set<Long>> recent) {
        var thisRound = new HashSet<Long>();

        for (var g : groups) {
            for (var i = 0; i < g.size(); i++) {
                for (var j = i + 1; j < g.size(); j++) {
                    var a = indexById.get(g.get(i));
                    var b = indexById.get(g.get(j));
                    pairCnt[min(a, b)][max(a, b)]++;
                    thisRound.add(pairKey(a, b));
                }
            }
        }
        recent.addFirst(thisRound);
        while (recent.size() > max(0, window)) {
            recent.removeLast();
        }
    }

    private int pairImbalance(int[][] pairCnt) {
        var min = Integer.MAX_VALUE;
        var max = Integer.MIN_VALUE;

        for (var i = 0; i < pairCnt.length; i++) {
            for (var j = i + 1; j < pairCnt.length; j++) {
                final var c = pairCnt[i][j];
                if (c < min) {
                    min = c;
                }
                if (c > max) {
                    max = c;
                }
            }
        }
        return max - min;
    }

    private long pairKey(int a, int b) {
        var i = min(a, b);
        var j = max(a, b);
        return ((long) i << 32) | (long) j;
    }

    private static final class Result {

        List<List<List<UUID>>> schedule;
        long finalScore;

        Result(List<List<List<UUID>>> schedule, long finalScore) {
            this.schedule = schedule;
            this.finalScore = finalScore;
        }
    }

    private static final class Round {

        boolean success;
        List<List<UUID>> groups;
        Set<Long> pairs;
        long deltaScore;

        private Round(boolean success, List<List<UUID>> groups, Set<Long> pairs, long deltaScore) {
            this.success = success;
            this.groups = groups;
            this.pairs = pairs;
            this.deltaScore = deltaScore;
        }

        static Round success(List<List<UUID>> groups, Set<Long> pairs, long delta) {
            return new Round(true, groups, pairs, delta);
        }

        static Round fail() {
            return new Round(false, List.of(), Set.of(), Long.MAX_VALUE);
        }
    }

    private record Choice(int groupIndex, long deltaScore) {

    }

    private List<ConstellationEntry> persistSchedule(List<List<List<UUID>>> schedule, UUID constellationId) {
        var result = new ArrayList<ConstellationEntry>();
        for (int roundIndex = 0; roundIndex < schedule.size(); roundIndex++) {
            var round = schedule.get(roundIndex);
            for (int groupIndex = 0; groupIndex < round.size(); groupIndex++) {
                var roundNumber = roundIndex + 1;
                var groupNumber = groupIndex + 1;
                var group = round.get(groupIndex);
                var entry = ConstellationEntry.builder()
                                              .id(UUID.randomUUID())
                                              .constellationId(constellationId)
                                              .roundNumber(roundNumber)
                                              .groupNumber(groupNumber)
                                              .personIds(group.stream().map(UUID::toString).collect(joining(",")))
                                              .build();
                constellationDatabaseFacade.saveConstellationEntry(entry);
                result.add(entry);
            }
        }
        return result;
    }

}
