package me.oskar.spl.analysis.name;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.error.Error;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;

public class NameAnalysis {

    private final boolean showTables;

    public NameAnalysis(boolean showTables) {
        this.showTables = showTables;
    }

    public SymbolTable buildSymbolTable(Program program, Error error, Target target, boolean headless) {
        var globalTable = target.tableInitializer.initializeGlobalTable(target, headless);
        var nameAnalysisVisitor = new NameAnalysisVisitor(globalTable, showTables, error);
        program.accept(nameAnalysisVisitor);

        return globalTable;
    }

    public static void printSymbolTableAtEndOfProcedure(String name, ProcedureEntry entry) {
        System.out.format("Symbol table at end of procedure '%s':\n", name);
        System.out.println(entry.getLocalTable().toString());
    }
}
