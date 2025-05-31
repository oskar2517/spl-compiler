package me.oskar.spl.ast;

import me.oskar.spl.position.Span;
import me.oskar.spl.type.Type;

public abstract class TypeExpression extends Node {

    @Node.NoProperty
    public Type dataType = null;

    public TypeExpression(Span span) {
        super(span);
    }
}
