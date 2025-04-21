package me.oskar.spl.table;

import me.oskar.spl.type.Type;

public class ParameterType {

    private final Type type;
    private final boolean reference;
    private VariablePosition position;

    public ParameterType(Type type, boolean reference) {
        this.type = type;
        this.reference = reference;
    }

    public ParameterType(Type type, boolean reference, VariablePosition position) {
        this.type = type;
        this.reference = reference;
        this.position = position;
    }

    public Type getType() {
        return type;
    }

    public boolean isReference() {
        return reference;
    }

    public VariablePosition getPosition() {
        return position;
    }

    public void setPosition(VariablePosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("%s%s", reference ? "ref " : "", this.type);
    }
}
