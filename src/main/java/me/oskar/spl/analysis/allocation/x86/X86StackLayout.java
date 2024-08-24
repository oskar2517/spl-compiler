package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.analysis.allocation.StackLayout;

public class X86StackLayout implements StackLayout {

    private final static int ALIGNMENT = 16;

    public int localVariableAreaSize = 0;
    public int argumentAreaSize = 0;
    public int outgoingAreaSize = 0;

    private int align(int size) {
        return (size + (ALIGNMENT - 1)) & -ALIGNMENT;
    }

    @Override
    public int getFrameSize() {
        return align(localVariableAreaSize + outgoingAreaSize);
    }
}
