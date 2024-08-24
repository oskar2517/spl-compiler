package me.oskar.spl.table;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class Levenshtein {

    private record Candidate(String ident, int value) {
    }

    public static String findMostLikelyCandidate(String ident, Class<?> expectedEntryType, SymbolTable symbolTable) {
        var candidate = symbolTable.recursiveEntrySet()
                .stream()
                .filter(e -> e.getValue().getClass().isAssignableFrom(expectedEntryType))
                .map(Map.Entry::getKey)
                .map(name -> new Candidate(name, calculateDistance(name, ident)))
                .min(Comparator.comparingInt(c -> c.value))
                .orElse(null);

        if (candidate != null && candidate.value <= Integer.max(ident.length(), 3) / 3) {
            return candidate.ident;
        }

        return null;
    }
    
    static int calculateDistance(String a, String b) {
        var distanceTable = new int[a.length() + 1][b.length() + 1];

        for (var i = 0; i <= a.length(); i++) {
            for (var j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    distanceTable[i][j] = j;
                } else if (j == 0) {
                    distanceTable[i][j] = i;
                } else {
                    distanceTable[i][j] = min(distanceTable[i - 1][j - 1]
                                    + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            distanceTable[i - 1][j] + 1,
                            distanceTable[i][j - 1] + 1);
                }
            }
        }

        return distanceTable[a.length()][b.length()];
    }
    private static int min(int... a) {
        return Arrays.stream(a)
                .min().orElse(Integer.MAX_VALUE);
    }
}
