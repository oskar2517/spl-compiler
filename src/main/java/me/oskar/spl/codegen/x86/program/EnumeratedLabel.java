package me.oskar.spl.codegen.x86.program;

public class EnumeratedLabel extends Label {

    private final int index;

    EnumeratedLabel(int index) {
        super(String.valueOf(index));

        this.index = index;
    }

    public EnumeratedLabel next() {
        return new EnumeratedLabel(index + 1);
    }
}
