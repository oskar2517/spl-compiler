package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {

    public final List<GlobalDeclaration> declarations = new ArrayList<>();

    public Program(Span span) {
        super(span);
    }

    public void addGlobalDeclaration(GlobalDeclaration globalDeclaration) {
        declarations.add(globalDeclaration);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("Program", declarations.toArray());
    }
}

