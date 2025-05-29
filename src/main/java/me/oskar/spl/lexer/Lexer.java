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

    private Span span(Position startPosition, int length) {
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
            case ';' -> new Token(TokenType.SEMICOLON, ";", span(startPosition, 1));
            case ',' -> new Token(TokenType.COMMA, ",", span(startPosition, 1));
            case '(' -> new Token(TokenType.L_PAREN, "(", span(startPosition, 1));
            case ')' -> new Token(TokenType.R_PAREN, ")", span(startPosition, 1));
            case '{' -> new Token(TokenType.L_CURL, "{", span(startPosition, 1));
            case '}' -> new Token(TokenType.R_CURL, "}", span(startPosition, 1));
            case '[' -> new Token(TokenType.L_BRACK, "[", span(startPosition, 1));
            case ']' -> new Token(TokenType.R_BRACK, "]", span(startPosition, 1));
            case '+' -> new Token(TokenType.PLUS, "+", span(startPosition, 1));
            case '-' -> new Token(TokenType.MINUS, "-", span(startPosition, 1));
            case '*' -> new Token(TokenType.ASTERISK, "*", span(startPosition, 1));
            case '/' -> new Token(TokenType.SLASH, "/", span(startPosition, 1));
            case '#' -> new Token(TokenType.HASH, "#", span(startPosition, 1));
            case '=' -> new Token(TokenType.EQUAL, "=", span(startPosition, 1));
            case ':' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.ASSIGN, ":=", span(startPosition, 2));
                } else {
                    yield new Token(TokenType.COLON, ":", span(startPosition, 1));
                }
            }
            case '<' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.LESS_THAN_EQUAL, "<=", span(startPosition, 2));
                } else {
                    yield new Token(TokenType.LESS_THAN, "<", span(startPosition, 1));
                }
            }
            case '>' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.GREATER_THAN_EQUAL, ">=", span(startPosition, 2));
                } else {
                    yield new Token(TokenType.GREATER_THAN, ">", span(startPosition, 1));
                }
            }
            case '\'' -> {
                var literal = readCharLiteral();
                if (literal == null) {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), span(startPosition, 3));
                }
                yield new Token(TokenType.CHAR, literal, span(startPosition, literal.equals("\n") ? 4 : 3));
            }
            case EOF -> new Token(TokenType.EOF, String.valueOf(EOF), span(startPosition, 0));
            default -> {
                if (currentChar == '0' && readChar() == 'x') {
                    var literal = readHexadecimalInteger();
                    yield new Token(TokenType.INT, literal, span(startPosition, literal.length()));
                } else if (Character.isDigit(currentChar)) {
                    var literal = readDecimalInteger();
                    yield new Token(TokenType.INT, literal, span(startPosition, literal.length()));
                } else if (isAlphanumeric(currentChar)) {
                    var ident = readIdent();
                    if (Keyword.isKeyword(ident)) {
                        yield new Token(Keyword.resolve(ident), ident, span(startPosition, ident.length()));
                    } else {
                        yield new Token(TokenType.IDENT, ident, span(startPosition, ident.length()));
                    }
                } else {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), span(startPosition, 1));
                }
            }
        };
    }
}
