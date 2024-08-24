package me.oskar.spl.type;

public abstract class Type {

    private final int byteSize;

    public int getByteSize() {
        return byteSize;
    }

    protected Type(int byteSize) {
        this.byteSize = byteSize;
    }
}


