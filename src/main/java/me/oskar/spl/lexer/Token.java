package me.oskar.spl.lexer;

public class Token {

    public record Position(int line, int lineOffset) {

        public static final Position basePosition = new Position(1, 0);

        @Override
        public String toString() {
            return "(%s, %s)".formatted(line, lineOffset);
        }
    }

    private final String lexeme;
    private final TokenType type;
    private final Position position;

    protected Token(TokenType type, String lexeme, Position position) {
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
    }

    @Override
    public String toString() {
        return "(%s. %s, %s)".formatted(type, lexeme.equals("\n") ? "\\n" : lexeme, position);
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }
}
