package me.oskar.spl.lexer;

import java.util.Map;

public class Keyword {

    static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.WHILE),
            Map.entry("array", TokenType.ARRAY),
            Map.entry("of", TokenType.OF),
            Map.entry("proc", TokenType.PROC),
            Map.entry("ref", TokenType.REF),
            Map.entry("type", TokenType.TYPE),
            Map.entry("var", TokenType.VAR)
    );

    public static boolean isKeyword(final String literal) {
        return keywords.containsKey(literal);
    }

    public static TokenType resolve(final String literal) {
        return keywords.get(literal);
    }
}
