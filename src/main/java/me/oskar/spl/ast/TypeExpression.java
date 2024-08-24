package me.oskar.spl.ast;

import me.oskar.spl.lexer.Token;
import me.oskar.spl.type.Type;

public abstract class TypeExpression extends Node {

    public Type dataType = null;

    public TypeExpression(Token.Position position) {
        super(position);
    }
}
