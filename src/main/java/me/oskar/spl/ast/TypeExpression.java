package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;
import me.oskar.spl.type.Type;

public abstract class TypeExpression extends Node {

    public static class Invalid extends TypeExpression {

        public Invalid(Span span) {
            super(span);
        }

        public void accept(Visitor visitor) {
            throw new IllegalStateException("Attempted to visit invalid node");
        }
    }

    @Node.NoProperty
    public Type dataType = null;

    public TypeExpression(Span span) {
        super(span);
    }
}
