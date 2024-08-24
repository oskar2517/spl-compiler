package me.oskar.spl.ast;

import me.oskar.spl.lexer.Token;
import me.oskar.spl.type.Type;

public abstract class Expression extends Node {

    public Type dataType = null;

    public Expression(Token.Position position) {
        super(position);
    }
}
