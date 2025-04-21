package me.oskar.spl.error;

import me.oskar.spl.ast.*;
import me.oskar.spl.lexer.Token;
import me.oskar.spl.lexer.TokenType;
import me.oskar.spl.type.Type;

public class Error {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final String code;
    private final String filename;

    public Error(String code, String filename) {
        this.code = code;
        this.filename = filename;
    }

    public static void notEnoughRegisters() {
        System.out.println("error: not enough registers available");
        System.exit(1);
    }

    private static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void printCode(Token.Position errorPosition, int underlineLength, String underlineMessage) {
        var codeLines = code.lines().toList();

        var codePreviewStart = Math.max(errorPosition.line() - 2, 1) - 1;
        var codePreviewEnd = Math.min(errorPosition.line() + 2, codeLines.size());

        var lineCountWidth = String.valueOf(errorPosition.line() + 3).length();

        for (var i = codePreviewStart; i < codePreviewEnd; i++) {
            var lineCount = padLeft(String.valueOf(i + 1), lineCountWidth);
            var codeLine = codeLines.get(i);

            System.out.printf("   %s | %s%n", lineCount, codeLine);

            if (i == errorPosition.line() - 1) {
                var underlineString = ANSI_RED +
                        "      " +
                        " ".repeat(lineCountWidth) +
                        " ".repeat(errorPosition.lineOffset());
                if (underlineLength > -1) {
                    underlineString += "^".repeat(underlineLength);
                } else {
                    underlineString += "^";
                }

                underlineString += " " + underlineMessage + ANSI_RESET;

                System.out.println(underlineString);
            }
        }
    }

    private void printCode(Token.Position errorPosition, int underlineLength) {
        printCode(errorPosition, underlineLength, "");
    }

    private void printErrorHead(Token.Position position, String message) {
        System.out.printf("%s%s:%s:%s%s %serror:%s %s%n", ANSI_BOLD, filename, position.line(), position.lineOffset() + 1,
                ANSI_RESET, ANSI_RED, ANSI_RESET, message);
    }

    public void missingSemicolon(Token token) {
        printErrorHead(token.getPosition(), "missing semicolon");
        printCode(token.getPosition(), -1, "expected semicolon after previous statement");
    }

    public void unexpectedToken(Token token, String expected) {
        if (token.getType() == TokenType.EOF) {
            printErrorHead(token.getPosition(), "unexpected end of file");
        } else {
            printErrorHead(token.getPosition(), "unexpected token");
            printCode(token.getPosition(), token.getLiteral().length(),
                    String.format("found `%s`, expected %s", token.getType().tokenName, expected));
        }
    }

    public void integerCannotBeParsed(Token token) {
        printErrorHead(token.getPosition(), "cannot parse integer");
        printCode(token.getPosition(), token.getLiteral().length(), "exceeds 32-bit integer");
    }

    public void redeclarationAsType(TypeDeclaration typeDeclaration) {
        printErrorHead(typeDeclaration.position, String.format("re-declaration of symbol `%s` as type", typeDeclaration.name));
        printCode(typeDeclaration.position, typeDeclaration.name.length(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsParameter(ParameterDeclaration parameterDeclaration) {
        printErrorHead(parameterDeclaration.position, String.format("re-declaration of symbol `%s` as parameter", parameterDeclaration.name));
        printCode(parameterDeclaration.position, parameterDeclaration.name.length(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsVariable(VariableDeclaration variableDeclaration) {
        printErrorHead(variableDeclaration.position, String.format("re-declaration of symbol `%s` as variable", variableDeclaration.name));
        printCode(variableDeclaration.position, variableDeclaration.name.length(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsProcedure(ProcedureDeclaration procedureDeclaration) {
        printErrorHead(procedureDeclaration.position, String.format("re-declaration of symbol `%s` as procedure", procedureDeclaration.name));
        printCode(procedureDeclaration.position, procedureDeclaration.name.length(), "has already been declared");

        System.exit(1);
    }

    public void typeUndefined(NamedTypeExpression namedTypeExpression, String candidate) {
        printErrorHead(namedTypeExpression.position, String.format("use of undefined symbol `%s` as type", namedTypeExpression.name));
        if (candidate == null) {
            printCode(namedTypeExpression.position, namedTypeExpression.name.length(), "is undefined");
        } else {
            printCode(namedTypeExpression.position, namedTypeExpression.name.length(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void procedureUndefined(CallStatement callStatement, String candidate) {
        printErrorHead(callStatement.position, String.format("call of undefined symbol `%s`", callStatement.procedureName));
        if (candidate == null) {
            printCode(callStatement.position, callStatement.procedureName.length(), "is undefined");
        }else {
            printCode(callStatement.position, callStatement.procedureName.length(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void callOfNonProcedure(CallStatement callStatement) {
        printErrorHead(callStatement.position, String.format("call of non-procedure `%s`", callStatement.procedureName));
        printCode(callStatement.position, callStatement.procedureName.length(), "is not a procedure");

        System.exit(1);
    }

    public void wrongNumberOfArguments(CallStatement callStatement, int expectedArgumentsSize) {
        printErrorHead(callStatement.position, String.format("wrong number of arguments to procedure `%s`", callStatement.procedureName));
        printCode(callStatement.position, callStatement.procedureName.length(),
                String.format("arguments found %s, expected %s", callStatement.arguments.size(), expectedArgumentsSize));

        System.exit(1);
    }

    public void argumentMustBeAVariable(CallStatement callStatement, int argument) {
        printErrorHead(callStatement.arguments.get(argument - 1).position, String.format("argument %s to procedure `%s` must be a variable",
                argument, callStatement.procedureName));
        printCode(callStatement.arguments.get(argument - 1).position, -1, "expected variable");

        System.exit(1);
    }

    public void argumentTypeMismatch(CallStatement callStatement, int argument, Type expected, Type found) {
        printErrorHead(callStatement.arguments.get(argument - 1).position, String.format("type mismatch in argument %s to procedure `%s`",
                argument, callStatement.procedureName));
        printCode(callStatement.arguments.get(argument - 1).position, -1, String.format("found `%s`, expected %s", found, expected));

        System.exit(1);
    }

    public void notAType(NamedTypeExpression namedTypeExpression) {
        printErrorHead(namedTypeExpression.position, String.format("`%s` is not a type", namedTypeExpression.name));
        printCode(namedTypeExpression.position, namedTypeExpression.name.length(), "expected type");

        System.exit(1);
    }

    public void mustBeReferenceParameter(ParameterDeclaration parameterDeclaration) {
        printErrorHead(parameterDeclaration.position, String.format("parameter `%s` must be a reference parameter",
                parameterDeclaration.name));
        printCode(parameterDeclaration.position, parameterDeclaration.name.length(), "must be a reference parameter");

        System.exit(1);
    }

    public void variableUndefined(NamedVariable namedVariable, String candidate) {
        printErrorHead(namedVariable.position, String.format("cannot find symbol `%s` in this scope",
                namedVariable.name));
        if (candidate == null) {
            printCode(namedVariable.position, namedVariable.name.length(), "is undefined");
        } else {
            printCode(namedVariable.position, namedVariable.name.length(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void notAVariable(NamedVariable namedVariable) {
        printErrorHead(namedVariable.position, String.format("cannot assign to non-variable `%s`",
                namedVariable.name));
        printCode(namedVariable.position, namedVariable.name.length(), "is not a variable");

        System.exit(1);
    }

    public void indexingNonArray(ArrayAccess arrayAccess) {
        printErrorHead(arrayAccess.position, "cannot index non-array");
        printCode(arrayAccess.position, -1, String.format("found `%s`, expected array", arrayAccess.array.dataType));

        System.exit(1);
    }

    public void expressionMismatchedType(Expression expression, Type expected) {
        printErrorHead(expression.position, "mismatched types");
        printCode(expression.position, -1, String.format("found `%s`, expected `%s`", expression.dataType, expected));

        System.exit(1);
    }

    public void mainMissing() {
        printErrorHead(Token.Position.basePosition, "main procedure is missing");

        System.exit(1);
    }

    public void mainNotAProcedure() {
        printErrorHead(Token.Position.basePosition, "symbol `main` is not a procedure");

        System.exit(1);
    }

    public void mainMustNotHaveParameters() {
        printErrorHead(Token.Position.basePosition, "main procedure must not have any parameters");

        System.exit(1);
    }
}
