package me.oskar.spl.codegen;

import me.oskar.spl.ast.Program;
import me.oskar.spl.table.SymbolTable;

import java.io.PrintWriter;

public interface CodeGenerator {
    void generateCode(Program program, SymbolTable symbolTable, PrintWriter printWriter, boolean headless);
}
