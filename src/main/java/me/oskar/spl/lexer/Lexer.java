package me.oskar.spl.lexer;

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

        var tokenPosition = new Token.Position(line, lineOffset);

        return switch (currentChar) {
            case ';' -> new Token(TokenType.SEMICOLON, ";", tokenPosition);
            case ',' -> new Token(TokenType.COMMA, ",", tokenPosition);
            case '(' -> new Token(TokenType.L_PAREN, "(", tokenPosition);
            case ')' -> new Token(TokenType.R_PAREN, ")", tokenPosition);
            case '{' -> new Token(TokenType.L_CURL, "{", tokenPosition);
            case '}' -> new Token(TokenType.R_CURL, "}", tokenPosition);
            case '[' -> new Token(TokenType.L_BRACK, "[", tokenPosition);
            case ']' -> new Token(TokenType.R_BRACK, "]", tokenPosition);
            case '+' -> new Token(TokenType.PLUS, "+", tokenPosition);
            case '-' -> new Token(TokenType.MINUS, "-", tokenPosition);
            case '*' -> new Token(TokenType.ASTERISK, "*", tokenPosition);
            case '/' -> new Token(TokenType.SLASH, "/", tokenPosition);
            case '#' -> new Token(TokenType.HASH, "#", tokenPosition);
            case '=' -> new Token(TokenType.EQUAL, "=", tokenPosition);
            case ':' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.ASSIGN, ":=", tokenPosition);
                } else {
                    yield new Token(TokenType.COLON, ":", tokenPosition);
                }
            }
            case '<' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.LESS_THAN_EQUAL, "<=", tokenPosition);
                } else {
                    yield new Token(TokenType.LESS_THAN, "<", tokenPosition);
                }
            }
            case '>' -> {
                if (readChar() == '=') {
                    nextChar();
                    yield new Token(TokenType.GREATER_THAN_EQUAL, ">=", tokenPosition);
                } else {
                    yield new Token(TokenType.GREATER_THAN, ">", tokenPosition);
                }
            }
            case '\'' -> {
                var literal = readCharLiteral();
                if (literal == null) {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), tokenPosition);
                }
                yield new Token(TokenType.CHAR, literal, tokenPosition);
            }
            case EOF -> new Token(TokenType.EOF, String.valueOf(EOF), tokenPosition);
            default -> {
                if (currentChar == '0' && readChar() == 'x') {
                    yield new Token(TokenType.INT, readHexadecimalInteger(), tokenPosition);
                } else if (Character.isDigit(currentChar)) {
                    yield new Token(TokenType.INT, readDecimalInteger(), tokenPosition);
                } else if (isAlphanumeric(currentChar)) {
                    var ident = readIdent();
                    if (Keyword.isKeyword(ident)) {
                        yield new Token(Keyword.resolve(ident), ident, tokenPosition);
                    } else {
                        yield new Token(TokenType.IDENT, ident, tokenPosition);
                    }
                } else {
                    yield new Token(TokenType.ILLEGAL, String.valueOf(currentChar), tokenPosition);
                }
            }
        };
    }
}
