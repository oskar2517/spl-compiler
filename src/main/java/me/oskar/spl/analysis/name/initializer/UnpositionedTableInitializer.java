package me.oskar.spl.analysis.name.initializer;

import me.oskar.spl.Target;
import me.oskar.spl.table.ParameterType;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.TypeEntry;

import java.util.List;

public class UnpositionedTableInitializer implements TableInitializer {

    @Override
    public SymbolTable initializeGlobalTable(Target target, boolean headless) {
        var table = new SymbolTable();
        enterPredefinedTypes(table, target);
        enterPredefinedProcedures(table, target, headless);
        return table;
    }

    private void enterPredefinedTypes(SymbolTable table, Target target) {
        table.enter("int", new TypeEntry(target.intType));
    }

    private static void enterPredefinedProcedures(SymbolTable table, Target target, boolean headless) {
        // printi(i: int)
        table.enter("printi", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false))));
        // printc(i: int)
        table.enter("printc", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false))));
        // readi(ref i: int)
        table.enter("readi", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true))));
        // readc(ref i: int)
        table.enter("readc", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true))));
        // exit()
        table.enter("exit", ProcedureEntry.predefinedProcedureEntry(List.of()));
        // time(ref i: int)
        table.enter("time", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true))));

        if (headless) {
            return;
        }
        // clearAll(color: int)
        table.enter("clearAll", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false))));
        // setPixel(x: int, y: int, color: int)
        table.enter("setPixel", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false))));
        // drawLine(x1: int, y1: int, x2: int, y2: int, color: int)
        table.enter("drawLine", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false))));
        // drawCircle(x0: int, y0: int, radius: int, color: int)
        table.enter("drawCircle", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false),
                new ParameterType(target.intType, false))));
    }
}
