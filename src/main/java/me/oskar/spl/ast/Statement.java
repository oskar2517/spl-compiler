package me.oskar.spl.ast;

import me.oskar.spl.position.Span;

public abstract class Statement extends Node {

    public Statement(Span span) {
        super(span);
    }
}
