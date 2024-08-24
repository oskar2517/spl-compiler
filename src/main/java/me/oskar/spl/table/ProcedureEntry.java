package me.oskar.spl.table;

import me.oskar.spl.analysis.allocation.StackLayout;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcedureEntry implements SymbolTableEntry {

    private final SymbolTable localTable;
    private final List<ParameterType> parameterTypes;
    private StackLayout stackLayout;

    public ProcedureEntry(SymbolTable localTable, List<ParameterType> parameterTypes) {
        this.localTable = localTable;
        this.parameterTypes = parameterTypes;
    }

    public static ProcedureEntry predefinedProcedureEntry(List<ParameterType> parameterTypes) {
        return new ProcedureEntry(null, parameterTypes);
    }

    public boolean isPredefinedProcedure() {
        return localTable == null;
    }

    public SymbolTable getLocalTable() {
        return localTable;
    }

    public StackLayout getStackLayout() {
        return stackLayout;
    }

    public void setStackLayout(StackLayout stackLayout) {
        this.stackLayout = stackLayout;
    }

    public List<ParameterType> getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String toString() {
        return String.format("proc: (%s)", this.parameterTypes.stream().map(Objects::toString).collect(Collectors.joining(", ")));
    }
}
