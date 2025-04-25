package me.oskar.spl.codegen.js;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;
import me.oskar.spl.type.Type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JsCodeGeneratorVisitor extends BaseVisitor {

    private SymbolTable symbolTable;
    private final CodePrinter output;
    private final Set<String> jsKeywords = new HashSet<>(Arrays.asList(
            "abstract", "arguments", "await", "boolean",
            "break", "byte", "case", "catch",
            "char", "class", "const", "continue",
            "debugger", "default", "delete", "do",
            "double", "else", "enum", "eval",
            "export", "extends", "false", "final",
            "finally", "float", "for", "function",
            "goto", "if", "implements", "import",
            "in", "instanceof", "int", "interface",
            "let", "long", "native", "new",
            "null", "package", "private", "protected",
            "public", "return", "short", "static",
            "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true",
            "try", "typeof", "var", "void",
            "volatile", "while", "with", "yield"
    ));

    private String prefixIdent(String ident) {
        return String.format("_%s", ident);
    }

    protected JsCodeGeneratorVisitor(SymbolTable symbolTable, CodePrinter output) {
        this.symbolTable = symbolTable;
        this.output = output;
    }

    private String resolveNameConflict(String name) {
        if (jsKeywords.contains(name)) {
            return String.format("_%s", name);
        } else {
            return name;
        }
    }

    private void generateArrayOfDimensions(Type type) {
        if (type instanceof ArrayType at) {
            output.printf("new Array(%s).fill().map(_ => ", at.getArraySize());
            generateArrayOfDimensions(at.getBaseType());
            output.print(")");
        } else {
            output.print("({ value: 0 })");
        }
    }

    private void generateCondition(Expression expression) {
        var condition = (BinaryExpression) expression;
        condition.leftOperand.accept(this);

        var operator = switch (condition.operator) {
            case BinaryExpression.Operator.EQUAL -> "===";
            case BinaryExpression.Operator.NOT_EQUAL -> "!==";
            case BinaryExpression.Operator.GREATER_THAN -> ">";
            case BinaryExpression.Operator.GREATER_THAN_EQUAL -> ">=";
            case BinaryExpression.Operator.LESS_THAN -> "<";
            case BinaryExpression.Operator.LESS_THAN_EQUAL -> "<=";
            default -> throw new IllegalStateException();
        };

        output.printf(" %s ", operator);

        condition.rightOperand.accept(this);
    }

    @Override
    public void visit(Program program) {
        for (var gd : program.declarations) {
            gd.accept(this);
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.target.accept(this);
        if (assignStatement.target instanceof NamedVariable nv) {
            var variableEntry = (VariableEntry) symbolTable.lookup(nv.name.symbol);

            if (variableEntry.isInMemory() || variableEntry.isReference()) {
                output.print(".value");
            }
        } else if (assignStatement.target instanceof ArrayAccess) {
            output.print(".value");
        }
        output.print(" = ");
        assignStatement.value.accept(this);
        output.println(";");
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);

        if (variableExpression.variable instanceof NamedVariable nv) {
            var variableEntry = (VariableEntry) symbolTable.lookup(nv.name.symbol);

            if (variableEntry.isInMemory() || variableEntry.isReference()) {
                output.print(".value");
            }
        } else if (variableExpression.variable instanceof ArrayAccess) {
            output.print(".value");
        }
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        output.print(resolveNameConflict(namedVariable.name.symbol));
    }

    @Override
    public void visit(CallStatement callStatement) {
        ProcedureEntry procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName.symbol);

        output.printf("await %s(", prefixIdent(callStatement.procedureName.symbol));

        for (var i = 0; i < callStatement.arguments.size(); i++) {
            Expression argument = callStatement.arguments.get(i);

            if (procedureEntry.getParameterTypes().get(i).isReference()) {
                var variableExpression = (VariableExpression) argument;
                variableExpression.variable.accept(this);
            } else {
                argument.accept(this);
            }

            if (i < callStatement.arguments.size() - 1) {
                output.print(", ");
            }
        }
        output.print(")");
        output.println(";");
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        output.print("(");

        binaryExpression.leftOperand.accept(this);

        var operator = switch (binaryExpression.operator) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            default -> throw new IllegalStateException();
        };
        output.printf(" %s ", operator);
        binaryExpression.rightOperand.accept(this);

        if (binaryExpression.operator == BinaryExpression.Operator.DIVIDE) {
            output.print(" | 0");
        }
        output.print(")");
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (unaryExpression.operator == UnaryExpression.Operator.NEGATE) {
            output.print("-(");
            unaryExpression.operand.accept(this);
            output.print(")");
        }
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        output.println("{");
        output.incIndentLevel();
        for (var s : compoundStatement.statements) {
            s.accept(this);
        }
        output.decIndentLevel();
        output.println("}");
    }

    @Override
    public void visit(IfStatement ifStatement) {
        var consequenceCompound = ifStatement.consequence instanceof CompoundStatement;
        var alternativeCompound = ifStatement.alternative instanceof CompoundStatement;

        output.print("if (");
        generateCondition(ifStatement.condition);
        output.println(")");
        if (!consequenceCompound) {
            output.incIndentLevel();
        }
        ifStatement.consequence.accept(this);
        if (!consequenceCompound) {
            output.decIndentLevel();
        }
        if (!(ifStatement.alternative instanceof EmptyStatement)) {
            output.println("else");
            if (!alternativeCompound) {
                output.incIndentLevel();
            }
            ifStatement.alternative.accept(this);
            if (!alternativeCompound) {
                output.decIndentLevel();
            }
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var bodyCompound = whileStatement.body instanceof CompoundStatement;

        output.print("while (");
        generateCondition(whileStatement.condition);
        output.println(")");
        if (!bodyCompound) {
            output.incIndentLevel();
        }
        whileStatement.body.accept(this);
        if (!bodyCompound) {
            output.decIndentLevel();
        }
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        output.printf("%s", intLiteral.value);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.array.accept(this);
        output.print("[checkArrayIndex(");
        arrayAccess.index.accept(this);
        output.printf(", %s)", ((ArrayType) arrayAccess.array.dataType).getArraySize());
        output.print("]");
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name.symbol);
        var lastTable = symbolTable;

        symbolTable = procedureEntry.getLocalTable();

        output.printf("async function %s(", prefixIdent(procedureDeclaration.name.symbol));

        for (var i = 0; i < procedureDeclaration.parameters.size(); i++) {
            var p = procedureDeclaration.parameters.get(i);
            output.print(resolveNameConflict(p.name.symbol));
            if (i < procedureDeclaration.parameters.size() - 1) {
                output.print(", ");
            }
        }
        output.println(") {");
        output.incIndentLevel();

        for (var v : procedureDeclaration.variables) {
            var variableEntry = (VariableEntry) symbolTable.lookup(v.name.symbol);

            if (variableEntry.getType() instanceof ArrayType at) {
                output.printf("let %s = ", resolveNameConflict(v.name.symbol));
                generateArrayOfDimensions(at);
                output.println(";");
            } else if (variableEntry.isInMemory()) {
                output.println("let %s = { value: 0 };", resolveNameConflict(v.name.symbol));
            } else {
                output.println("let %s = 0;", resolveNameConflict(v.name.symbol));
            }
        }

        for (var p : procedureDeclaration.parameters) {
            var variableEntry = (VariableEntry) symbolTable.lookup(p.name.symbol);

            if (variableEntry.isInMemory()) {
                output.println("%s = { value: %s };", resolveNameConflict(p.name.symbol),
                        resolveNameConflict(p.name.symbol));
            }
        }


        for (var s : procedureDeclaration.body) {
            s.accept(this);
        }

        output.decIndentLevel();
        output.println("}");
        output.println();

        symbolTable = lastTable;
    }
}
