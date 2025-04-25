package me.oskar.spl.lexer;

import me.oskar.spl.position.Span;

public class Token {

    private final String lexeme;
    private final TokenType type;
    private final Span span;

    protected Token(TokenType type, String lexeme, Span span) {
        this.type = type;
        this.lexeme = lexeme;
        this.span = span;
    }

    @Override
    public String toString() {
        return "(%s. %s, %s)".formatted(type, lexeme.equals("\n") ? "\\n" : lexeme, span);
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public Span span() {
        return span;
    }
}
