package me.oskar.spl.analysis.name.initializer;

import me.oskar.spl.analysis.allocation.x86.RegisterPosition;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.table.ParameterType;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.TypeEntry;

import java.util.List;

public class X86TableInitializer implements TableInitializer {

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
                new ParameterType(target.intType, false, new RegisterPosition("rdi")))));
        // printc(i: int)
        table.enter("printc", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false, new RegisterPosition("rdi")))));
        // readi(ref i: int)
        table.enter("readi", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true, new RegisterPosition("rdi")))));
        // readc(ref i: int)
        table.enter("readc", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true, new RegisterPosition("rdi")))));
        // exit()
        table.enter("exit", ProcedureEntry.predefinedProcedureEntry(List.of()));
        // time(ref i: int)
        table.enter("time", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, true, new RegisterPosition("rdi")))));

        if (headless) {
            return;
        }
        // clearAll(color: int)
        table.enter("clearAll", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false, new RegisterPosition("rdi")))));
        // setPixel(x: int, y: int, color: int)
        table.enter("setPixel", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false, new RegisterPosition("rdi")),
                new ParameterType(target.intType, false, new RegisterPosition("rsi")),
                new ParameterType(target.intType, false, new RegisterPosition("rdx")))));
        // drawLine(x1: int, y1: int, x2: int, y2: int, color: int)
        table.enter("drawLine", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false, new RegisterPosition("rdi")),
                new ParameterType(target.intType, false, new RegisterPosition("rsi")),
                new ParameterType(target.intType, false, new RegisterPosition("rdx")),
                new ParameterType(target.intType, false, new RegisterPosition("rcx")),
                new ParameterType(target.intType, false, new RegisterPosition("r8")))));
        // drawCircle(x0: int, y0: int, radius: int, color: int)
        table.enter("drawCircle", ProcedureEntry.predefinedProcedureEntry(List.of(
                new ParameterType(target.intType, false, new RegisterPosition("rdi")),
                new ParameterType(target.intType, false, new RegisterPosition("rsi")),
                new ParameterType(target.intType, false, new RegisterPosition("rdx")),
                new ParameterType(target.intType, false, new RegisterPosition("rcx")))));
    }
}