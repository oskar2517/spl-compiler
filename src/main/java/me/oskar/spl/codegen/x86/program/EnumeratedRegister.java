package me.oskar.spl.codegen.x86.program;

import me.oskar.spl.error.Error;

public class EnumeratedRegister {

    private final int index;
    private static final Register[] registers = new Register[] {
            Register.R10,
            Register.R11,
            Register.R12,
            Register.R13,
            Register.R14,
            Register.R15,
            Register.RBX
    };

    public EnumeratedRegister(int index) {
        this.index = index;
    }

    public EnumeratedRegister next() {
        return new EnumeratedRegister(index + 1);
    }

    public EnumeratedRegister previous() {
        return new EnumeratedRegister(index - 1);
    }

    @Override
    public String toString() {
        try {
            return registers[index].toString();
        } catch (IndexOutOfBoundsException e) {
            Error.notEnoughRegisters();
            return null; // unreachable
        }
    }
}
