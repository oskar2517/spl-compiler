package me.oskar.spl.analysis.allocation.wasm;

import me.oskar.spl.analysis.allocation.VariableAllocation;
import me.oskar.spl.ast.Program;
import me.oskar.spl.Target;
import me.oskar.spl.table.SymbolTable;

public class WasmVariableAllocation implements VariableAllocation {

    @Override
    public void allocateVariables(Program program, SymbolTable symbolTable, Target target) {
        var variableAllocationVisitor = new WasmVariableAllocationVisitor(symbolTable, target);
        program.accept(variableAllocationVisitor);
    }
}
