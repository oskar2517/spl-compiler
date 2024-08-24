package me.oskar.spl.table;

import me.oskar.spl.type.Type;

public class TypeEntry implements SymbolTableEntry {

    private final Type type;

    public TypeEntry(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("type: %s", type);
    }
}

