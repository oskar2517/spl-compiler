package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public abstract class GlobalDeclaration extends Node {

    public static class Invalid extends GlobalDeclaration {

        public Invalid(Span span) {
            super(span, null);
        }

        public void accept(Visitor visitor) {
            throw new IllegalStateException("Attempted to visit invalid node");
        }
    }

    public final Identifier name;

    public GlobalDeclaration(Span span, Identifier name) {
        super(span);

        this.name = name;
    }
}

