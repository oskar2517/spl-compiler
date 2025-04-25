package me.oskar.spl.ast.invalid;

import me.oskar.spl.ast.Statement;
import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class InvalidStatement extends Statement {

    public InvalidStatement(Span span) {
        super(span);
    }

    @Override
    public void accept(Visitor visitor) {
        throw new IllegalStateException("Attempted to visit invalid node");
    }
}
