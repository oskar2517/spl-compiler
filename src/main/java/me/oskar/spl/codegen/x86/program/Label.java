package me.oskar.spl.codegen.x86.program;

public class Label implements Operand {

    private final String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "L" + name;
    }
}
