package me.oskar.spl.analysis.allocation;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.table.SymbolTable;

public interface VariableAllocation {

    void allocateVariables(Program program, SymbolTable symbolTable, Target target);
}
