package me.oskar.spl.table;

import me.oskar.spl.type.Type;

public class VariableEntry implements SymbolTableEntry {

    private final Type type;
    private final boolean reference;
    private boolean inMemory = false;
    private VariablePosition position;

    public VariableEntry(Type type, boolean reference) {
        this.type = type;
        this.reference = reference;
    }

    public Type getType() {
        return type;
    }

    public boolean isReference() {
        return reference;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }

    public VariablePosition getPosition() {
        return position;
    }

    public void setPosition(VariablePosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("var: %s%s %s", reference ? "ref " : "",
                inMemory ? String.format("position %s", position) : "", type);
    }
}

