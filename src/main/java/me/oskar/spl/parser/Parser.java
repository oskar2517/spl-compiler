package me.oskar.spl.parser;

import me.oskar.spl.ast.*;
import me.oskar.spl.error.Error;
import me.oskar.spl.lexer.Lexer;
import me.oskar.spl.lexer.Token;
import me.oskar.spl.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Parser {

    private final Lexer lexer;
    private final Error error;
    private Token currentToken;
    private final Program program;
    private boolean errorMode = false;
    private boolean panic = false;

    public Parser(Lexer lexer, Error error) {
        this.lexer = lexer;
        this.error = error;

        nextToken();
        program = new Program(currentToken.getPosition());
    }

    public Program getProgram() {
        return program;
    }

    private void nextToken() {
        currentToken = lexer.nextToken();
    }

    private void reportAndRecover(Runnable errorHandler, AnchorSet anc) {
        panic = true;
        if (!errorMode) {
            errorHandler.run();
            errorMode = true;
        }
        while (!anc.contains(currentToken.getType())) {
            nextToken();
        }
    }

    private void expectToken(TokenType expectedType, String message, AnchorSet anc) {
        if (currentToken.getType() == expectedType) {
            errorMode = false;
            nextToken();
            return;
        }

        panic = true;

        if (!errorMode) {
            error.unexpectedToken(currentToken, message);
            errorMode = true;
        }

        while (!anc.contains(currentToken.getType()) && currentToken.getType() != expectedType) {
            nextToken();
        }

        if (currentToken.getType() == expectedType) {
            nextToken();
        }
    }

    private void eatSemicolon(AnchorSet anc) {
        expectToken(TokenType.SEMICOLON, "semicolon", anc);
    }

    private void eatToken(final TokenType type, AnchorSet anc) {
        expectToken(type, String.format("`%s`", type.tokenName), anc);
    }

    public void generateAst() {
        var anc = AnchorSet.of(TokenType.EOF);

        while (currentToken.getType() != TokenType.EOF) {
            parseGlobal(anc.union(FirstRules.GLOBAL_DECLARATION_FIRST));
        }

        if (panic) {
            System.exit(1);
        }
    }

    private void parseGlobal(AnchorSet anc) {
        switch (currentToken.getType()) {
            case TYPE -> program.addGlobalDeclaration(parseTypeDeclaration(anc));
            case PROC -> program.addGlobalDeclaration(parseProcedureDeclaration(anc));
            default -> reportAndRecover(() -> error.unexpectedToken(currentToken,
                    "type declaration or procedure declaration"), anc);
        }
    }

    private TypeDeclaration parseTypeDeclaration(AnchorSet anc) {
        eatToken(TokenType.TYPE, anc.add(TokenType.IDENT, TokenType.EQUAL, TokenType.ARRAY, TokenType.IDENT,
                TokenType.SEMICOLON));

        var name = currentToken.getLiteral();
        var position = currentToken.getPosition();
        eatToken(TokenType.IDENT, anc.add(TokenType.EQUAL, TokenType.ARRAY, TokenType.IDENT, TokenType.SEMICOLON));

        eatToken(TokenType.EQUAL, anc.add(TokenType.ARRAY, TokenType.IDENT, TokenType.SEMICOLON));

        var typeExpression = parseTypeExpression(anc.add(TokenType.SEMICOLON));

        eatSemicolon(anc);

        return new TypeDeclaration(position, name, typeExpression);
    }

    private TypeExpression parseTypeExpression(AnchorSet anc) {
        var position = currentToken.getPosition();

        return switch (currentToken.getType()) {
            case ARRAY -> {
                eatToken(TokenType.ARRAY, anc.add(TokenType.L_BRACK, TokenType.INT, TokenType.CHAR, TokenType.R_BRACK,
                        TokenType.OF));
                eatToken(TokenType.L_BRACK, anc.add(TokenType.INT, TokenType.CHAR, TokenType.R_BRACK, TokenType.OF));

                int arraySize;
                if (currentToken.getType() == TokenType.INT) {
                    arraySize = Integer.decode(currentToken.getLiteral());
                    eatToken(TokenType.INT, anc.add(TokenType.R_BRACK, TokenType.OF));
                } else {
                    arraySize = currentToken.getLiteral().charAt(0);
                    eatToken(TokenType.CHAR, anc.add(TokenType.R_BRACK, TokenType.OF));
                }

                eatToken(TokenType.R_BRACK, anc.add(TokenType.OF));

                eatToken(TokenType.OF, anc);
                var baseType = parseTypeExpression(anc);

                yield new ArrayTypeExpression(position, baseType, arraySize);
            }
            case IDENT -> {
                var name = currentToken.getLiteral();
                eatToken(TokenType.IDENT, anc);
                yield new NamedTypeExpression(position, name);
            }
            default -> {
                reportAndRecover(() -> error.unexpectedToken(currentToken, "type expression"), anc);
                yield null;
            }
        };
    }

    private ProcedureDeclaration parseProcedureDeclaration(AnchorSet anc) {
        eatToken(TokenType.PROC, anc.add(TokenType.IDENT, TokenType.L_PAREN, TokenType.R_PAREN, TokenType.L_CURL,
                TokenType.VAR, TokenType.R_CURL).union(FirstRules.PARAMETER_DECLARATION_FIRST)
                .union(FirstRules.STATEMENT_FIRST));

        var procedureName = currentToken.getLiteral();
        var position = currentToken.getPosition();
        eatToken(TokenType.IDENT, anc.add(TokenType.L_PAREN, TokenType.R_PAREN, TokenType.L_CURL, TokenType.VAR,
                TokenType.R_CURL).union(FirstRules.PARAMETER_DECLARATION_FIRST).union(FirstRules.STATEMENT_FIRST));
        eatToken(TokenType.L_PAREN, anc.add(TokenType.R_PAREN, TokenType.L_CURL, TokenType.VAR, TokenType.R_CURL)
                .union(FirstRules.PARAMETER_DECLARATION_FIRST).union(FirstRules.STATEMENT_FIRST));

        var parameterAnc = anc.add(TokenType.R_PAREN, TokenType.L_CURL, TokenType.VAR, TokenType.R_CURL)
                .union(FirstRules.STATEMENT_FIRST);
        var parameters = parseNodeList(() -> parseParameterDeclaration(parameterAnc.add(TokenType.COMMA)), TokenType.R_PAREN, parameterAnc);

        eatToken(TokenType.R_PAREN, anc.add(TokenType.L_CURL, TokenType.VAR, TokenType.R_CURL)
                .union(FirstRules.STATEMENT_FIRST));
        eatToken(TokenType.L_CURL, anc.add(TokenType.VAR, TokenType.R_CURL).union(FirstRules.STATEMENT_FIRST));

        var variableDeclarations = new ArrayList<VariableDeclaration>();
        while (currentToken.getType() == TokenType.VAR) {
            var variableDeclaration = parseVariableDeclaration(anc.add(TokenType.VAR, TokenType.R_CURL)
                    .union(FirstRules.STATEMENT_FIRST));
            variableDeclarations.add(variableDeclaration);
        }

        var statements = new ArrayList<Statement>();
        while (currentToken.getType() != TokenType.R_CURL && currentToken.getType() != TokenType.EOF) {
            var statement = parseStatement(anc.add(TokenType.R_CURL).union(FirstRules.STATEMENT_FIRST));
            statements.add(statement);
        }

        eatToken(TokenType.R_CURL, anc);

        return new ProcedureDeclaration(position, procedureName, parameters, variableDeclarations, statements);
    }

    private <T> List<T> parseNodeList(Supplier<T> nodeListFunction, TokenType endToken, AnchorSet anc) {
        var list = new ArrayList<T>();

        while (currentToken.getType() != endToken && currentToken.getType() != TokenType.EOF) {
            var element = nodeListFunction.get();
            list.add(element);

            if (lexer.peekToken().getType() == endToken && currentToken.getType() == TokenType.COMMA) { // Trailing comma
                eatToken(TokenType.R_PAREN, anc.add(TokenType.COMMA));
            } else if (currentToken.getType() != endToken) {
                eatToken(TokenType.COMMA, anc.add(TokenType.COMMA));
            }
        }

        return list;
    }

    private Statement parseStatement(AnchorSet anc) {
        return switch (currentToken.getType()) {
            case IF -> parseIfStatement(anc);
            case WHILE -> parseWhileStatement(anc);
            case SEMICOLON -> parseEmptyStatement(anc);
            case L_CURL -> parseCompoundStatement(anc);
            case IDENT -> {
                if (lexer.peekToken().getType() == TokenType.L_PAREN) {
                    yield parseCallStatement(anc);
                } else {
                    yield parseAssignStatement(anc);
                }
            }
            default -> {
                reportAndRecover(() -> error.unexpectedToken(currentToken, "statement"), anc);
                yield null;
            }
        };
    }

    private Statement parseIfStatement(AnchorSet anc) {
        var position = currentToken.getPosition();

        eatToken(TokenType.IF, anc.add(TokenType.L_PAREN, TokenType.R_PAREN).union(FirstRules.EXPRESSION_FIRST)
                .union(FirstRules.STATEMENT_FIRST));
        eatToken(TokenType.L_PAREN, anc.add(TokenType.R_PAREN).union(FirstRules.EXPRESSION_FIRST)
                .union(FirstRules.STATEMENT_FIRST));

        var condition = parseExpression(anc.add(TokenType.R_PAREN).union(FirstRules.STATEMENT_FIRST));

        eatToken(TokenType.R_PAREN, anc.union(FirstRules.STATEMENT_FIRST));

        var consequence = parseStatement(anc);
        Statement alternative = new EmptyStatement(position);

        if (currentToken.getType() == TokenType.ELSE) {
            eatToken(TokenType.ELSE, anc.union(FirstRules.STATEMENT_FIRST));

            alternative = parseStatement(anc);
        }

        return new IfStatement(position, condition, consequence, alternative);
    }

    private Statement parseWhileStatement(AnchorSet anc) {
        var position = currentToken.getPosition();

        eatToken(TokenType.WHILE, anc.add(TokenType.L_PAREN, TokenType.R_PAREN).union(FirstRules.EXPRESSION_FIRST)
                .union(FirstRules.STATEMENT_FIRST));
        eatToken(TokenType.L_PAREN, anc.add(TokenType.R_PAREN).union(FirstRules.EXPRESSION_FIRST)
                .union(FirstRules.STATEMENT_FIRST));

        var condition = parseExpression(anc.add(TokenType.R_PAREN).union(FirstRules.STATEMENT_FIRST));

        eatToken(TokenType.R_PAREN, anc.union(FirstRules.STATEMENT_FIRST));

        var body = parseStatement(anc);

        return new WhileStatement(position, condition, body);
    }

    private Statement parseEmptyStatement(AnchorSet anc) {
        var position = currentToken.getPosition();
        eatSemicolon(anc);

        return new EmptyStatement(position);
    }

    private Statement parseCallStatement(AnchorSet anc) {
        var position = currentToken.getPosition();

        var name = currentToken.getLiteral();
        eatToken(TokenType.IDENT, anc.add(TokenType.L_PAREN, TokenType.R_PAREN, TokenType.SEMICOLON)
                .union(FirstRules.EXPRESSION_FIRST));
        eatToken(TokenType.L_PAREN, anc.add(TokenType.R_PAREN, TokenType.SEMICOLON)
                .union(FirstRules.EXPRESSION_FIRST));

        var argumentsAnc = anc.add(TokenType.R_PAREN, TokenType.SEMICOLON);
        var arguments = parseNodeList(() -> parseExpression(argumentsAnc), TokenType.R_PAREN, argumentsAnc);

        eatToken(TokenType.R_PAREN, anc.add(TokenType.SEMICOLON));
        eatSemicolon(anc);

        return new CallStatement(position, name, arguments);
    }

    private Statement parseAssignStatement(AnchorSet anc) {
        var variable = parseVariable(anc.add(TokenType.ASSIGN, TokenType.SEMICOLON)
                .union(FirstRules.EXPRESSION_FIRST));

        var position = currentToken.getPosition();
        eatToken(TokenType.ASSIGN, anc.add(TokenType.SEMICOLON).union(FirstRules.EXPRESSION_FIRST));

        var value = parseExpression(anc.add(TokenType.SEMICOLON));
        eatSemicolon(anc);

        return new AssignStatement(position, variable, value);
    }

    private VariableDeclaration parseVariableDeclaration(AnchorSet anc) {
        eatToken(TokenType.VAR, anc.add(TokenType.IDENT, TokenType.COLON, TokenType.SEMICOLON)
                .union(FirstRules.TYPE_EXPRESSION_FIRST));

        var name = currentToken.getLiteral();
        var position = currentToken.getPosition();
        eatToken(TokenType.IDENT, anc.add(TokenType.COLON, TokenType.SEMICOLON)
                .union(FirstRules.TYPE_EXPRESSION_FIRST));
        eatToken(TokenType.COLON, anc.add(TokenType.SEMICOLON).union(FirstRules.TYPE_EXPRESSION_FIRST));

        var type = parseTypeExpression(anc.add(TokenType.SEMICOLON));
        eatSemicolon(anc);

        return new VariableDeclaration(position, name, type);
    }

    private ParameterDeclaration parseParameterDeclaration(AnchorSet anc) {
        var position = currentToken.getPosition();
        var reference = false;

        if (currentToken.getType() == TokenType.REF) {
            eatToken(TokenType.REF, anc.add(TokenType.IDENT, TokenType.COLON).union(FirstRules.TYPE_EXPRESSION_FIRST));
            reference = true;
        }
        var name = currentToken.getLiteral();

        eatToken(TokenType.IDENT, anc.add(TokenType.COLON).union(FirstRules.TYPE_EXPRESSION_FIRST));
        eatToken(TokenType.COLON, anc.union(FirstRules.TYPE_EXPRESSION_FIRST));

        var type = parseTypeExpression(anc);

        return new ParameterDeclaration(position, name, type, reference);
    }

    private CompoundStatement parseCompoundStatement(AnchorSet anc) {
        var position = currentToken.getPosition();

        eatToken(TokenType.L_CURL, anc.add(TokenType.R_CURL).union(FirstRules.STATEMENT_FIRST));
        var statements = new ArrayList<Statement>();
        while (currentToken.getType() != TokenType.R_CURL && currentToken.getType() != TokenType.EOF) {
            var statement = parseStatement(anc.add(TokenType.R_CURL).union(FirstRules.STATEMENT_FIRST));
            statements.add(statement);
        }


        eatToken(TokenType.R_CURL, anc);

        return new CompoundStatement(position, statements);
    }

    private Expression parseExpression(AnchorSet anc) {
        return parseComparison(anc);
    }

    private Expression parseComparison(AnchorSet anc) {
        var left = parseNumeric(anc);

        while (true) {
            BinaryExpression.Operator type;
            var operator = currentToken;

            switch (operator.getType()) {
                case LESS_THEN -> type = BinaryExpression.Operator.LESS_THEN;
                case LESS_THEN_EQUAL -> type = BinaryExpression.Operator.LESS_THEN_EQUAL;
                case GREATER_THEN -> type = BinaryExpression.Operator.GREATER_THEN;
                case GREATER_THEN_EQUAL -> type = BinaryExpression.Operator.GREATER_THEN_EQUAL;
                case EQUAL -> type = BinaryExpression.Operator.EQUAL;
                case HASH -> type = BinaryExpression.Operator.NOT_EQUAL;
                default -> {
                    return left;
                }
            }

            nextToken();
            final var right = parseNumeric(anc);
            left = new BinaryExpression(operator.getPosition(), type, left, right);
        }
    }

    private Expression parseNumeric(AnchorSet anc) {
        var left = parseTerm(anc);

        while (true) {
            BinaryExpression.Operator type;
            var operator = currentToken;

            switch (operator.getType()) {
                case PLUS -> type = BinaryExpression.Operator.ADD;
                case MINUS -> type = BinaryExpression.Operator.SUBTRACT;
                default -> {
                    return left;
                }
            }

            nextToken();
            final var right = parseTerm(anc);
            left = new BinaryExpression(operator.getPosition(), type, left, right);
        }
    }

    private Expression parseTerm(AnchorSet anc) {
        var left = parseSignedFactor(anc);

        while (true) {
            BinaryExpression.Operator type;
            var operator = currentToken;

            switch (operator.getType()) {
                case ASTERISK -> type = BinaryExpression.Operator.MULTIPLY;
                case SLASH -> type = BinaryExpression.Operator.DIVIDE;
                default -> {
                    return left;
                }
            }

            nextToken();
            final var right = parseSignedFactor(anc);
            left = new BinaryExpression(operator.getPosition(), type, left, right);
        }
    }

    private Expression parseSignedFactor(AnchorSet anc) {
        var operator = currentToken;

        return switch (operator.getType()) {
            case MINUS -> {
                eatToken(TokenType.MINUS, anc);

                final var right = parseFactor(anc);
                yield new UnaryExpression(operator.getPosition(), UnaryExpression.Operator.NEGATE, right);
            }
            default -> parseFactor(anc);
        };
    }

    private Expression parseFactor(AnchorSet anc) {
        var token = currentToken;

        return switch (token.getType()) {
            case IDENT -> new VariableExpression(token.getPosition(), parseVariable(anc));
            case INT -> {
                try {
                    var intLiteral = new IntLiteral(token.getPosition(), Integer.decode(token.getLiteral()));
                    eatToken(TokenType.INT, anc);

                    yield intLiteral;
                } catch (NumberFormatException e) {
                    reportAndRecover(() -> error.integerCannotBeParsed(token), anc);
                    yield null;
                }
            }
            case CHAR -> {
                var charLiteral = new IntLiteral(token.getPosition(), token.getLiteral().charAt(0));
                eatToken(TokenType.CHAR, anc);

                yield charLiteral;
            }
            case L_PAREN -> {
                eatToken(TokenType.L_PAREN, anc);
                final var comp = parseComparison(anc);
                eatToken(TokenType.R_PAREN, anc);

                yield comp;
            }
            default -> {
                reportAndRecover(() -> error.unexpectedToken(currentToken, "expression"), anc);
                yield null;
            }
        };
    }

    private Variable parseVariable(AnchorSet anc) {
        Variable left = new NamedVariable(currentToken.getPosition(), currentToken.getLiteral());
        eatToken(TokenType.IDENT, anc.add(TokenType.L_BRACK, TokenType.R_BRACK).union(FirstRules.EXPRESSION_FIRST));

        while (currentToken.getType() == TokenType.L_BRACK) {
            var position = currentToken.getPosition();

            eatToken(TokenType.L_BRACK, anc.add(TokenType.R_BRACK).union(FirstRules.EXPRESSION_FIRST));

            var index = parseExpression(anc.add(TokenType.R_BRACK));

            eatToken(TokenType.R_BRACK, anc);

            left = new ArrayAccess(position, left, index);
        }

        return left;
    }
}