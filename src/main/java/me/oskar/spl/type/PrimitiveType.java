package me.oskar.spl.type;

public class PrimitiveType extends Type {

    private final String printName;

    public PrimitiveType(int byteSize, String printName) {
        super(byteSize);
        this.printName = printName;
    }

    @Override
    public String toString() {
        return printName;
    }
}

