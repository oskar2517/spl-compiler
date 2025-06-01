package me.oskar.spl.error;

import me.oskar.spl.ast.*;
import me.oskar.spl.lexer.Token;
import me.oskar.spl.lexer.TokenType;
import me.oskar.spl.position.Span;
import me.oskar.spl.type.Type;

import java.util.List;

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

    private final List<String> code;
    private final String filename;

    public Error(String code, String filename) {
        this.code = code.lines().toList();
        this.filename = filename;
    }

    public static void notEnoughRegisters() {
        System.out.println("error: not enough registers available");
        System.exit(1);
    }

    private static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }


    private void printUnderline(int offset, int length, String underlineMessage) {
        var s = ANSI_RED + "      " + " ".repeat(offset) + "^".repeat(length) + " " + underlineMessage + ANSI_RESET;

        System.out.println(s);
    }

    private void printErrorLine(int startOffset, int endOffset, String lineCount, String codeLine) {
        var startPart = codeLine.substring(0, startOffset);
        var errorPart = codeLine.substring(startOffset, endOffset);
        var endPart = codeLine.substring(endOffset);

        var s = startPart + ANSI_RED + errorPart + ANSI_RESET + endPart;

        System.out.printf("   %s | %s%n", lineCount, s);
    }

    private void printCode(Span span, String underlineMessage) {
        var codePreviewStart = Math.max(span.start().line() - 2, 1);
        var codePreviewEnd = Math.min(span.end().line() + 3, code.size());

        var lineCountWidth = String.valueOf(span.end().line() + 3).length();

        for (var i = codePreviewStart; i <= codePreviewEnd; i++) {
            var lineCount = padLeft(String.valueOf(i), lineCountWidth);
            var codeLine = code.get(i - 1);

            if (span.includesLine(i)) {
                if (span.isMultiline()) {
                    if (i == span.start().line()) {
                        var startOffset = span.start().lineOffset();
                        var endOffset = codeLine.length();
                        printErrorLine(startOffset, endOffset, lineCount, codeLine);
                    } else if (i == span.end().line()) {
                        var startOffset = 0;
                        var endOffset = span.end().lineOffset();
                        printErrorLine(startOffset, endOffset, lineCount, codeLine);
                    } else {
                        printErrorLine(0, codeLine.length(), lineCount, codeLine);
                    }
                } else {
                    printErrorLine(span.start().lineOffset(), span.end().lineOffset(), lineCount, codeLine);
                }
            } else {
                System.out.printf("   %s | %s%n", lineCount, codeLine);
            }

            if (span.isMultiline()) {
                if (i == span.start().line()) {
                    var offset = lineCountWidth + span.start().lineOffset();
                    printUnderline(offset, codeLine.length() - span.start().lineOffset(), "");
                } else if (i == span.end().line()) {
                    printUnderline(lineCountWidth, span.end().lineOffset(), underlineMessage);
                } else if (span.includesLine(i)) {
                    printUnderline(lineCountWidth, codeLine.length(), "");
                }
            } else if (i == span.start().line()) {
                var offset = lineCountWidth + span.start().lineOffset();
                var length = span.end().lineOffset() - span.start().lineOffset();
                printUnderline(offset, length, underlineMessage);
            }
        }
    }

    private void printErrorHead(Span span, String message) {
        System.out.printf("%s%s:%s%s %serror:%s %s%n", ANSI_BOLD, filename, span.start().line(), ANSI_RESET, ANSI_RED,
                ANSI_RESET, message);
    }

    public void unexpectedToken(Token token, String expected) {
        if (token.getType() == TokenType.EOF) {
            printErrorHead(token.span(), "unexpected end of file");
        } else {
            printErrorHead(token.span(), "unexpected token");
            printCode(token.span(), String.format("found `%s`, expected %s", token.getType().tokenName, expected));
        }
    }

    public void integerCannotBeParsed(Token token) {
        printErrorHead(token.span(), "cannot parse integer");
        printCode(token.span(), "exceeds 32-bit integer");
    }

    public void redeclarationAsType(TypeDeclaration typeDeclaration) {
        printErrorHead(typeDeclaration.span(), String.format("re-declaration of symbol `%s` as type",
                typeDeclaration.name.symbol));
        printCode(typeDeclaration.name.span(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsParameter(ParameterDeclaration parameterDeclaration) {
        printErrorHead(parameterDeclaration.span(), String.format("re-declaration of symbol `%s` as parameter",
                parameterDeclaration.name.symbol));
        printCode(parameterDeclaration.name.span(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsVariable(VariableDeclaration variableDeclaration) {
        printErrorHead(variableDeclaration.span(), String.format("re-declaration of symbol `%s` as variable",
                variableDeclaration.name.symbol));
        printCode(variableDeclaration.name.span(), "has already been declared");

        System.exit(1);
    }

    public void redeclarationAsProcedure(ProcedureDeclaration procedureDeclaration) {
        printErrorHead(procedureDeclaration.span(), String.format("re-declaration of symbol `%s` as procedure",
                procedureDeclaration.name.symbol));
        printCode(procedureDeclaration.name.span(), "has already been declared");

        System.exit(1);
    }

    public void typeUndefined(NamedTypeExpression namedTypeExpression, String candidate) {
        printErrorHead(namedTypeExpression.name.span(), String.format("use of undefined symbol `%s` as type",
                namedTypeExpression.name.symbol));
        if (candidate == null) {
            printCode(namedTypeExpression.name.span(), "is undefined");
        } else {
            printCode(namedTypeExpression.name.span(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void procedureUndefined(CallStatement callStatement, String candidate) {
        printErrorHead(callStatement.span(), String.format("call of undefined symbol `%s`",
                callStatement.procedureName.symbol));
        if (candidate == null) {
            printCode(callStatement.procedureName.span(), "is undefined");
        }else {
            printCode(callStatement.procedureName.span(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void callOfNonProcedure(CallStatement callStatement) {
        printErrorHead(callStatement.span(), String.format("call of non-procedure `%s`",
                callStatement.procedureName.symbol));
        printCode(callStatement.procedureName.span(), "is not a procedure");

        System.exit(1);
    }

    public void wrongNumberOfArguments(CallStatement callStatement, int expectedArgumentsSize) {
        printErrorHead(callStatement.span(), String.format("wrong number of arguments to procedure `%s`",
                callStatement.procedureName));
        printCode(callStatement.procedureName.span(), String.format("arguments found %s, expected %s",
                callStatement.arguments.size(), expectedArgumentsSize));

        System.exit(1);
    }

    public void argumentMustBeAVariable(CallStatement callStatement, int argument) {
        printErrorHead(callStatement.arguments.get(argument - 1).span(),
                String.format("argument %s to procedure `%s` must be a variable",
                argument, callStatement.procedureName.symbol));
        printCode(callStatement.arguments.get(argument - 1).span(), "expected variable");

        System.exit(1);
    }

    public void argumentTypeMismatch(CallStatement callStatement, int argument, Type expected, Type found) {
        printErrorHead(callStatement.arguments.get(argument - 1).span(),
                String.format("type mismatch in argument %s to procedure `%s`", argument,
                        callStatement.procedureName.symbol));
        printCode(callStatement.arguments.get(argument - 1).span(),
                String.format("found `%s`, expected `%s`", found, expected));

        System.exit(1);
    }

    public void notAType(NamedTypeExpression namedTypeExpression) {
        printErrorHead(namedTypeExpression.span(), String.format("`%s` is not a type",
                namedTypeExpression.name.symbol));
        printCode(namedTypeExpression.span(), "expected type");

        System.exit(1);
    }

    public void mustBeReferenceParameter(ParameterDeclaration parameterDeclaration) {
        printErrorHead(parameterDeclaration.span(), String.format("parameter `%s` must be a reference parameter",
                parameterDeclaration.name.symbol));
        printCode(parameterDeclaration.name.span(), "must be a reference parameter");

        System.exit(1);
    }

    public void variableUndefined(NamedVariable namedVariable, String candidate) {
        printErrorHead(namedVariable.span(), String.format("cannot find symbol `%s` in this scope",
                namedVariable.name.symbol));
        if (candidate == null) {
            printCode(namedVariable.name.span(), "is undefined");
        } else {
            printCode(namedVariable.name.span(), String.format("did you mean `%s`?", candidate));
        }

        System.exit(1);
    }

    public void notAVariable(NamedVariable namedVariable) {
        printErrorHead(namedVariable.span(), String.format("cannot assign to non-variable `%s`",
                namedVariable.name.symbol));
        printCode(namedVariable.name.span(), "is not a variable");

        System.exit(1);
    }

    public void indexingNonArray(ArrayAccess arrayAccess) {
        printErrorHead(arrayAccess.span(), "cannot index non-array");
        printCode(arrayAccess.span(), String.format("found `%s`, expected array", arrayAccess.array.dataType));

        System.exit(1);
    }

    public void expressionMismatchedType(Expression expression, Type expected) {
        printErrorHead(expression.span(), "mismatched types");
        printCode(expression.span(), String.format("found `%s`, expected `%s`", expression.dataType, expected));

        System.exit(1);
    }

    public void mainMissing() {
        printErrorHead(Span.BASE_SPAN, "main procedure is missing");

        System.exit(1);
    }

    public void mainNotAProcedure() {
        printErrorHead(Span.BASE_SPAN, "symbol `main` is not a procedure");

        System.exit(1);
    }

    public void mainMustNotHaveParameters() {
        printErrorHead(Span.BASE_SPAN, "main procedure must not have any parameters");

        System.exit(1);
    }
}
