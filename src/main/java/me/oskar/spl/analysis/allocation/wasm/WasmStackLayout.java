package me.oskar.spl.analysis.allocation.wasm;

import me.oskar.spl.analysis.allocation.StackLayout;

public class WasmStackLayout implements StackLayout {

    public int localVariableAreaSize;

    @Override
    public int getFrameSize() {
        return localVariableAreaSize;
    }
}
