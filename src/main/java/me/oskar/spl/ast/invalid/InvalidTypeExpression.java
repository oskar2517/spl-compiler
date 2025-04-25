package me.oskar.spl.ast.invalid;

import me.oskar.spl.ast.TypeExpression;
import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class InvalidTypeExpression extends TypeExpression {

    public InvalidTypeExpression(Span span) {
        super(span);
    }

    @Override
    public void accept(Visitor visitor) {
        throw new IllegalStateException("Attempted to visit invalid node");
    }
}
