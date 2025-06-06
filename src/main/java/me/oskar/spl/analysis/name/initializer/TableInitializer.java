package me.oskar.spl.analysis.name.initializer;

import me.oskar.spl.Target;
import me.oskar.spl.table.SymbolTable;

public interface TableInitializer {

    SymbolTable initializeGlobalTable(Target target, boolean headless);
}
