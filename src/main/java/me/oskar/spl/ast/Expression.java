package me.oskar.spl.ast;

import me.oskar.spl.position.Span;
import me.oskar.spl.type.Type;

public abstract class Expression extends Node {

    public Type dataType = null;

    public Expression(Span span) {
        super(span);
    }
}
