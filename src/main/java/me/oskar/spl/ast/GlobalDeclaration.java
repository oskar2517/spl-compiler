package me.oskar.spl.ast;

import me.oskar.spl.position.Span;

public abstract class GlobalDeclaration extends Node {

    public final Identifier name;

    public GlobalDeclaration(Span span, Identifier name) {
        super(span);

        this.name = name;
    }
}

