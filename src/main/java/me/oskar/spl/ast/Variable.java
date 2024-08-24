package me.oskar.spl.ast;

import me.oskar.spl.lexer.Token;
import me.oskar.spl.type.Type;

public abstract class Variable extends Node {

    public Type dataType = null;

    public Variable(Token.Position position) {
        super(position);
    }
}

