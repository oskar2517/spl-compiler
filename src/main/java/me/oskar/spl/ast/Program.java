package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {

    public final List<GlobalDeclaration> declarations;

    public Program(Span span, List<GlobalDeclaration> declarations) {
        super(span);

        this.declarations = declarations;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

