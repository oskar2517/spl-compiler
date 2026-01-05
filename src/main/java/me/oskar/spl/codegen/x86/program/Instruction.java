package me.oskar.spl.codegen.x86.program;

import java.util.List;
import java.util.stream.Collectors;

record Instruction(OpCode opCode, List<Operand> operands) {

    @Override
    public String toString() {
        return opCode + " " + operands.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
