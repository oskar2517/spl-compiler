package me.oskar.spl.codegen.x86;

import me.oskar.spl.error.Error;

public class Register {

    private final int index;
    private static final String[] registers = new String[] {"r10", "r11", "r12", "r13", "r14", "r15", "rbx"};

    protected Register(int index) {
        this.index = index;
    }

    public Register next() {
        return new Register(index + 1);
    }

    public Register previous() {
        return new Register(index - 1);
    }

    @Override
    public String toString() {
        try {
            return registers[index];
        } catch (IndexOutOfBoundsException e) {
            Error.notEnoughRegisters();
            return null; // unreachable
        }
    }
}
