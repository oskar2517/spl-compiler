package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public abstract class Statement extends Node {

    public static class Invalid extends Statement {

        public Invalid(Span span) {
            super(span);
        }

        public void accept(Visitor visitor) {
            throw new IllegalStateException("Attempted to visit invalid node");
        }
    }

    public Statement(Span span) {
        super(span);
    }
}
