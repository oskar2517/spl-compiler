package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.analysis.allocation.VariableAllocation;
import me.oskar.spl.ast.Program;
import me.oskar.spl.Target;
import me.oskar.spl.table.SymbolTable;

public class X86VariableAllocation implements VariableAllocation {

    @Override
    public void allocateVariables(Program program, SymbolTable symbolTable, Target target) {
        var variableAllocationVisitor = new X86VariableAllocationVisitor(symbolTable, target);
        program.accept(variableAllocationVisitor);
    }
}
