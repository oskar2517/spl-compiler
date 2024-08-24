package me.oskar.spl.analysis.semantic;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.error.Error;
import me.oskar.spl.table.SymbolTable;

public class SemanticAnalysis {

    public void checkProcedures(Program program, SymbolTable globalTable, Error error, Target target) {
        var semanticAnalysisVisitor = new SemanticAnalysisVisitor(globalTable, error, target);
        program.accept(semanticAnalysisVisitor);
    }
}
