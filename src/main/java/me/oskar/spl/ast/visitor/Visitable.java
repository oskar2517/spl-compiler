package me.oskar.spl.ast.visitor;

public interface Visitable {

    void accept(Visitor visitor);
}
