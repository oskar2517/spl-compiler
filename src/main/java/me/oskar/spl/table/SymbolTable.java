package me.oskar.spl.table;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SymbolTable {

    private final Map<String, SymbolTableEntry> entries = new HashMap<>();
    private final SymbolTable parentTable;

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
    }

    public SymbolTable() {
        parentTable = null;
    }

    public void enter(String name, SymbolTableEntry entry) {
        entries.putIfAbsent(name, entry);
    }

    public void enter(String name, SymbolTableEntry entry, Runnable error) {
        if (entries.containsKey(name)) {
            error.run();
            return;
        }

        entries.put(name, entry);
    }

    public SymbolTableEntry lookup(String name) {
        var entry = entries.get(name);

        if (entry != null) return entry;
        if (parentTable != null) return parentTable.lookup(name);
        return null;
    }

    public SymbolTableEntry lookup(String name, Runnable error) {
        var entry = lookup(name);
        if (entry == null) {
            error.run();
            return null;
        }

        return entry;
    }

    public SymbolTableEntry lookup(String name, Class<?> expectedEntryType, Consumer<String> error) {
        var entry = lookup(name);
        if (entry == null) {
            var candidate = Levenshtein.findMostLikelyCandidate(name, expectedEntryType, this);
            error.accept(candidate);
            return null;
        }

        return entry;
    }

    public Set<Map.Entry<String, SymbolTableEntry>> entrySet() {
        return entries.entrySet();
    }

    public Set<Map.Entry<String, SymbolTableEntry>> recursiveEntrySet() {
        var entrySet = new HashSet<>(entries.entrySet());
        if (parentTable != null) {
            entrySet.addAll(parentTable.recursiveEntrySet());
        }

        return entrySet;
    }

    public String generateStringRepresentation(int level) {
        var string = String.format("  level %d\n", level);

        if (entries.isEmpty()) string += "    <empty>\n";
        else {
            string += entries.entrySet().stream()
                    .sorted(Comparator.comparing(a -> a.getKey().toString()))
                    .map(entry -> String.format("    %-15s --> %s\n", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(""));
        }

        if (parentTable != null) string += parentTable.generateStringRepresentation(level + 1);

        return string;
    }

    @Override
    public String toString() {
        return generateStringRepresentation(0);
    }
}

