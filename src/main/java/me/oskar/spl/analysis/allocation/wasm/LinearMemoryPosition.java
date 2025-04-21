package me.oskar.spl.analysis.allocation.wasm;

import me.oskar.spl.table.VariablePosition;

public class LinearMemoryPosition implements VariablePosition {

    private final int offset;

    LinearMemoryPosition(int offset) {
        this.offset = offset;
    }

    @Override
    public String getRealPosition() {
        return String.valueOf(offset);
    }
}
