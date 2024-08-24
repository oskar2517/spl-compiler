package me.oskar.spl.ast;

import me.oskar.spl.lexer.Token;

public abstract class GlobalDeclaration extends Node {

    public final String name;

    public GlobalDeclaration(Token.Position position, String name) {
        super(position);

        this.name = name;
    }
}

