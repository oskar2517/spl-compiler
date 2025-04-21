package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.table.VariablePosition;

public class RegisterPosition implements VariablePosition {

    private final String name;

    public RegisterPosition(String name) {
        this.name = name;
    }

    @Override
    public String getRealPosition() {
        return name;
    }
}
