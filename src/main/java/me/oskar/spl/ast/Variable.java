package me.oskar.spl.ast;

import me.oskar.spl.position.Span;
import me.oskar.spl.type.Type;

public abstract class Variable extends Node {

    @Node.NoProperty
    public Type dataType = null;

    public Variable(Span span) {
        super(span);
    }
}

