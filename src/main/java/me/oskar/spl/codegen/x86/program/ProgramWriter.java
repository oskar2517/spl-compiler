package me.oskar.spl.codegen.x86.program;

import me.oskar.spl.codegen.CodePrinter;

import java.io.PrintWriter;
import java.util.Arrays;

public class ProgramWriter {

    private final CodePrinter output;

    public ProgramWriter(PrintWriter printWriter) {
        this.output = new CodePrinter(printWriter, 4);
    }

    public void label(EnumeratedLabel label) {
        output.resetIndentLevel();
        output.println(label.toString() + ":");
        output.incIndentLevel();
    }

    public void label(EnumeratedLabel label, String data) {
        output.resetIndentLevel();
        output.println("%s: %s", label, data);
        output.incIndentLevel();
    }

    public void ins(OpCode opCode, Operand... operands) {
        output.println(new Instruction(opCode, Arrays.stream(operands).toList()).toString());
    }

    public void extern(String name) {
        output.println("extern " + name);
    }

    public void section(String name, String... attributes) {
        output.println("%s %s", name, String.join(" ", attributes));
    }

    public void emptyLine() {
        output.println();
    }
}
