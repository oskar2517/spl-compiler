package me.oskar.spl.analysis.allocation.js;

import me.oskar.spl.analysis.allocation.VariableAllocation;
import me.oskar.spl.ast.Program;
import me.oskar.spl.Target;
import me.oskar.spl.table.SymbolTable;

public class JsVariableAllocation  implements VariableAllocation {

    @Override
    public void allocateVariables(Program program, SymbolTable symbolTable, Target target) {
        var variableAllocationVisitor = new JsVariableAllocationVisitor(symbolTable, target);
        program.accept(variableAllocationVisitor);
    }
}
