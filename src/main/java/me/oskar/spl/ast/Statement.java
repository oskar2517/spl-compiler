package me.oskar.spl.ast;

import me.oskar.spl.lexer.Token;

public abstract class Statement extends Node {

    public Statement(Token.Position position) {
        super(position);
    }
}
