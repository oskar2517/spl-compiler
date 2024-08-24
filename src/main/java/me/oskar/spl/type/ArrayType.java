package me.oskar.spl.type;

public class ArrayType extends Type {

    private final Type baseType;
    private final int arraySize;

    public ArrayType(Type baseType, int arraySize) {
        super(arraySize * baseType.getByteSize());
        this.baseType = baseType;
        this.arraySize = arraySize;
    }

    public Type getBaseType() {
        return baseType;
    }

    public int getArraySize() {
        return arraySize;
    }

    @Override
    public String toString() {
        return String.format("array [%d] of %s", arraySize, baseType);
    }
}
