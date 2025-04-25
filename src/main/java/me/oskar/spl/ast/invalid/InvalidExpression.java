package me.oskar.spl.ast.invalid;

import me.oskar.spl.ast.Expression;
import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class InvalidExpression extends Expression {

    public InvalidExpression(Span span) {
        super(span);
    }

    @Override
    public void accept(Visitor visitor) {
        throw new IllegalStateException("Attempted to visit invalid node");
    }
}
