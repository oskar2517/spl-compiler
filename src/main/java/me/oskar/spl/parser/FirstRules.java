package me.oskar.spl.parser;

import me.oskar.spl.lexer.TokenType;

public class FirstRules {

    public static final AnchorSet GLOBAL_DECLARATION_FIRST = AnchorSet.of(TokenType.TYPE, TokenType.PROC);
    public static final AnchorSet PARAMETER_DECLARATION_FIRST = AnchorSet.of(TokenType.IDENT, TokenType.REF);
    public static final AnchorSet STATEMENT_FIRST = AnchorSet.of(TokenType.IDENT, TokenType.IF, TokenType.WHILE, TokenType.L_CURL, TokenType.SEMICOLON);
    public static final AnchorSet EXPRESSION_FIRST = AnchorSet.of(TokenType.IDENT, TokenType.INT, TokenType.CHAR, TokenType.L_PAREN, TokenType.MINUS);
    public static final AnchorSet TYPE_EXPRESSION_FIRST = AnchorSet.of(TokenType.ARRAY, TokenType.IDENT);
}
