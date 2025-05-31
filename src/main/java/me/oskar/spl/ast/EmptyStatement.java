package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class EmptyStatement extends Statement {

    public EmptyStatement(Span span) {
        super(span);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

