package me.oskar.spl.codegen.llvm;

record Global(String name) {

    @Override
    public String toString() {
        return "@_" + name;
    }
}
