package me.oskar.spl.codegen.llvm;

class Register {

    private final String name;

    public Register(String name) {
        this.name = name;
    }

    public String address() {
        return toString() + ".addr";
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}
