package me.oskar.spl.lexer;

public class Token {

    public record Position(int line, int lineOffset) {

        public static final Position basePosition = new Position(1, 0);

        @Override
        public String toString() {
            return "(" + line + ", " + lineOffset + ")";
        }
    }

    private final String literal;
    private final TokenType type;
    private final Position position;

    protected Token(TokenType type, String literal, Position position) {
        this.type = type;
        this.literal = literal;
        this.position = position;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + (literal.equals("\n") ? "\\n" : literal) + ", " + position + ")";
    }

    public String getLiteral() {
        return literal;
    }

    public TokenType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }
}
