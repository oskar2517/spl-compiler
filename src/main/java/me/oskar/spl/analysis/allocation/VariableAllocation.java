package me.oskar.spl.analysis.allocation;

import me.oskar.spl.ast.Program;
import me.oskar.spl.Target;
import me.oskar.spl.table.SymbolTable;

public interface VariableAllocation {

    class Unallocated implements VariableAllocation {

        @Override
        public void allocateVariables(Program program, SymbolTable symbolTable, Target target) {

        }
    }

    void allocateVariables(Program program, SymbolTable symbolTable, Target target);
}
