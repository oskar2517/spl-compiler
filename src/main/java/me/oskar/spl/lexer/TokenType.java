package me.oskar.spl.lexer;

public enum TokenType {
    // Expressions
    IDENT("identifier"),
    INT("integer"),
    CHAR("character"),

    // Keywords
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    ARRAY("array"),
    OF("of"),
    PROC("proc"),
    REF("ref"),
    TYPE("type"),
    VAR("var"),

    // Brackets
    L_PAREN("("),
    R_PAREN(")"),
    L_BRACK("["),
    R_BRACK("]"),
    L_CURL("{"),
    R_CURL("}"),

    // Operators
    EQUAL("="),
    HASH("#"),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),

    // Seperators
    COLON(":"),
    SEMICOLON(";"),
    COMMA(","),

    // Misc
    ASSIGN(":="),
    EOF("EOF"),
    ILLEGAL("");

    public final String tokenName;

    TokenType(String tokenName) {
        this.tokenName = tokenName;
    }
}
