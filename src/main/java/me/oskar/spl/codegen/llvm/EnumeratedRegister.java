package me.oskar.spl.codegen.llvm;

class EnumeratedRegister extends Register {

    private final int index;

    protected EnumeratedRegister(int index) {
        super(String.valueOf(index));

        this.index = index;
    }

    public EnumeratedRegister next() {
        return new EnumeratedRegister(index + 1);
    }
}

