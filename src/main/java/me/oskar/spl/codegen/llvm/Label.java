package me.oskar.spl.codegen.llvm;

class Label {

    private final int index;

    Label(int index) {
        this.index = index;
    }

    public Label next() {
        return new Label(index + 1);
    }

    @Override
    public String toString() {
        return String.format("L%s", index);
    }
}
