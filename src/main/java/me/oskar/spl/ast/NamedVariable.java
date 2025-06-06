package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class NamedVariable extends Variable {

    public final Identifier name;

    public NamedVariable(Span span, Identifier name) {
        super(span);

        this.name = name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

