package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class IntLiteral extends Expression {

    public final int value;

    public IntLiteral(Span span, int value) {
        super(span);

        this.value = value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

