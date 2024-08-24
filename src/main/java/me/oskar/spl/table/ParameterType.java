package me.oskar.spl.table;

import me.oskar.spl.type.Type;

public class ParameterType {

    private final Type type;
    private final boolean reference;
    private String position;

    public ParameterType(Type type, boolean reference) {
        this.type = type;
        this.reference = reference;
    }

    public ParameterType(Type type, boolean reference, String position) {
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("%s%s", reference ? "ref " : "", this.type);
    }
}
