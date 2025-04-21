package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.table.VariablePosition;

public class StackPosition implements VariablePosition {

    private final RegisterPosition relative;
    private final int offset;

    StackPosition(RegisterPosition relative, int offset) {
        this.relative = relative;
        this.offset = offset;
    }

    @Override
    public String getRealPosition() {
        return String.format("[%s+%s]", relative.getRealPosition(), offset);
    }
}
