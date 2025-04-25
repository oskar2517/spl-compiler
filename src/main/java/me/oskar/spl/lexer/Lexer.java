package me.oskar.spl.lexer;

import me.oskar.spl.position.Position;
import me.oskar.spl.position.Span;

public class Lexer {

    private static final char EOF = '\0';

    private final String code;
    private int position = 0;
    private int line = 1;
    private int lineOffset = -1;
    private char currentChar = EOF;

    public Lexer(String code) {
        this.code = code.replace("\t", "    ");
    }

    private boolean isAlphanumeric(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private boolean isHexadecimal(char c) {
        return (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || (c >= '0' && c <= '9');
    }

    private char readChar() {
        if (position < code.length()) {
            return code.charAt(position);
        } else {
            return EOF;
        }
    }

    private void updateLinePosition() {
        if (currentChar == '\n') {
            line++;
            lineOffset = -1;
        } else {
            lineOffset++;
        }
    }

    private void nextChar() {
        currentChar = readChar();
        updateLinePosition();
        position++;
    }

    private String readIdent() {
        var startPosition = position;
        while (isAlphanumeric(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private String readDecimalInteger() {
        var startPosition = position;
        while (Character.isDigit(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private String readHexadecimalInteger() {
        var startPosition = position;
        nextChar(); // Skip 0
        nextChar(); // Skip x
        while (isHexadecimal(readChar())) {
            nextChar();
        }

        return code.substring(startPosition - 1, position);
    }

    private String readCharLiteral() {
        nextChar(); // Skip first '

        char c;
        if (currentChar == '\\' && readChar() == 'n') {
            nextChar();
            c = '\n';
        } else {
            c = currentChar;
        }

        nextChar(); // Skip second '
        if (currentChar != '\'') {
            return null;
        }

        return String.valueOf(c);
    }

    private void eatWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private void eatComment() {
        if (currentChar != '/' || readChar() != '/') {
            return;
        }

        while (currentChar != '\n' && currentChar != EOF) {
            nextChar();
        }

        eatWhitespace();
        eatComment();
    }

    private Span endPosition(Position startPosition, int length) {
        return new Span(startPosition, new Position(startPosition.line(), startPosition.lineOffset() + length));
    }

    public void printTokens() {
        while (true) {
            var token = nextToken();
            System.out.println(token);
            if (token.getType() == TokenType.EOF) {
                break;
            }
        }
    }

    public Token peekToken() {
        var oldPosition = position;
        var oldCurrentChar = currentChar;
        var oldLine = line;
        var oldLineOffset = lineOffset;

        var token = nextToken();

        position = oldPosition;
        currentChar = oldCurrentChar;
        line = oldLine;
        lineOffset = oldLineOffset;

        return token;
    }

    public Token nextToken() {
        nextChar();
        eatWhitespace();
        eatComment();

        var startPosition = new Position(line, lineOffset);

        return switch (currentChar) {
            case ';' -> new Token(TokenType.SEMICOLON, ";", endPosition(startPosition, 1));
            case ',' -> new Token(TokenType.COMMA, ",", endPosition(startPosition, 1));
            case '(' -> new Token(TokenType.L_PAREN, "(", endPosition(startPosition, 1));
            case ')' -> new Token(TokenType.R_PAREN, ")", endPosition(startPosition, 1));
            case '{' -> new Token(TokenType.L_CURL, "{", endPosition(startPosition, 1));
            case '}' -> new Token(TokenType.R_CURL, "}", endPosition(startPosition, 1));
            case '[' -> new Token(TokenType.L_BRACK, "[", endPosition(startPosition, 1));
            case ']' -> new Token(TokenType.R_BRACK, "]", endPosition(startPosition, 1));
            case '+' -> new Token(TokenType.PLUS, "+", endPosition(startPosition, 1));
            case '-' -> new Token(TokenType.MINUS, "-", endPosition(startPosition, 1));
            case '*' -> new Token(TokenType.ASTERISK, "*", endPosition(startPosition, 1));
            case '/' -> new Token(TokenType.SLASH, "/", endPosition(startPosition, 1));
            case '#' -> new Token(TokenType.HASH, "#", endPosition(startPosition, 1));
            case '=' -> new Token(TokenType.EQUAL, "=",endPosition(startPosition, 1));
            case ':' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.ASSIGN, ":=", endPosition(startPosition, 2));
                } else {
                    yield new Token(TokenType.COLON, ":", endPosition(startPosition, 1));
                }
            }
            case '<' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.LESS_THAN_EQUAL, "<=", endPosition(startPosition, 2));
                } else {
                    yield new Token(TokenType.LESS_THAN, "<", endPosition(startPosition, 1));
                }
            }
            case '>' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.GREATER_THAN_EQUAL, ">=", endPosition(startPosition, 2));
                } else {
                    yield new Token(TokenType.GREATER_THAN, ">", endPosition(startPosition, 1));
                }
            }
            case '\'' -> {
                var literal = readCharLiteral();
                if (literal == null) {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), endPosition(startPosition, 3));
                }
                yield new Token(TokenType.CHAR, literal, endPosition(startPosition, literal.equals("\n") ? 4 : 3));
            }
            case EOF -> new Token(TokenType.EOF, String.valueOf(EOF), endPosition(startPosition, 0));
            default -> {
                if (currentChar == '0' && readChar() == 'x') {
                    var literal = readHexadecimalInteger();
                    yield new Token(TokenType.INT, literal, endPosition(startPosition, literal.length()));
                } else if (Character.isDigit(currentChar)) {
                    var literal = readDecimalInteger();
                    yield new Token(TokenType.INT, literal, endPosition(startPosition, literal.length()));
                } else if (isAlphanumeric(currentChar)) {
                    var ident = readIdent();
                    if (Keyword.isKeyword(ident)) {
                        yield new Token(Keyword.resolve(ident), ident, endPosition(startPosition, ident.length()));
                    } else {
                        yield new Token(TokenType.IDENT, ident, endPosition(startPosition, ident.length()));
                    }
                } else {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), endPosition(startPosition, 1));
                }
            }
        };
    }
}
