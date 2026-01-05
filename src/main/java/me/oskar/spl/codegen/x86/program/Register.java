package me.oskar.spl.codegen.x86.program;

public enum Register implements Operand {
    R10("r10"),
    R11("r11"),
    R12("r12"),
    R13("r13"),
    R14("r14"),
    R15("r15"),
    RBX("rbx")
    ;

    private final String name;

    Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
