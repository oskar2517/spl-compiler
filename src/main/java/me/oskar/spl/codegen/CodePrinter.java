package me.oskar.spl.codegen;

import java.io.PrintWriter;

public class CodePrinter {

    private final int indentWidth;
    private final PrintWriter outputFile;
    private boolean nextLine = false;
    private int indentLevel = 0;

    public CodePrinter(PrintWriter outputFile, int indentWidth) {
        this.outputFile = outputFile;
        this.indentWidth = indentWidth;
    }

    public void incIndentLevel() {
        indentLevel++;
    }

    public void decIndentLevel() {
        indentLevel--;
        assert indentLevel >= 0;
    }

    private void beginLine() {
        outputFile.print(" ".repeat(indentWidth * indentLevel));
    }

    public void printf(String format, Object... args) {
        if (nextLine) {
            nextLine = false;
            beginLine();
        }
        outputFile.printf(format, args);
    }

    public void println(String format, Object... args) {
        if (nextLine) {
            nextLine = false;
            beginLine();
        }
        outputFile.println(String.format(format, args));
        nextLine = true;
    }

    public void println() {
        if (nextLine) {
            nextLine = false;
            beginLine();
        }
        outputFile.println();
        nextLine = true;
    }

    public void print(String x) {
        if (nextLine) {
            nextLine = false;
            beginLine();
        }
        outputFile.print(x);
    }
}

