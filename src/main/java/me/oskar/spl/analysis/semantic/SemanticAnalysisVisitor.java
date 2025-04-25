package me.oskar.spl.analysis.semantic;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.Target;
import me.oskar.spl.error.Error;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;

public class SemanticAnalysisVisitor extends BaseVisitor {

    private final SymbolTable symbolTable;
    private final Error error;
    private final Target target;

    public SemanticAnalysisVisitor(SymbolTable symbolTable, Error error, Target target) {
        this.symbolTable = symbolTable;
        this.error = error;
        this.target = target;
    }

    @Override
    public void visit(Program program) {
        for (var decl : program.declarations) {
            if (decl instanceof ProcedureDeclaration) {
                decl.accept(this);
            }
        }
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name.symbol);
        var semanticAnalysisVisitor = new SemanticAnalysisVisitor(procEntry.getLocalTable(), error, target);

        for (var s : procedureDeclaration.body) {
            s.accept(semanticAnalysisVisitor);
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);

        var leftDataType = binaryExpression.leftOperand.dataType;
        var rightDataType = binaryExpression.rightOperand.dataType;

        if (leftDataType != target.intType) {
            error.expressionMismatchedType(binaryExpression.leftOperand, target.intType);
        }
        if (rightDataType != target.intType) {
            error.expressionMismatchedType(binaryExpression.rightOperand, target.intType);
        }

        switch (binaryExpression.operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE -> binaryExpression.dataType = target.intType;
            case LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, EQUAL, NOT_EQUAL ->
                    binaryExpression.dataType = target.boolType;
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.operand.accept(this);
        var dataType = unaryExpression.operand.dataType;

        if (dataType != target.intType) {
            error.expressionMismatchedType(unaryExpression.operand, target.intType);
        }

        unaryExpression.dataType = target.intType;
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        var entry = symbolTable.lookup(namedVariable.name.symbol, VariableEntry.class,
                (candidate) -> error.variableUndefined(namedVariable, candidate));

        if (!(entry instanceof VariableEntry)) {
            error.notAVariable(namedVariable);
            return;
        }

        namedVariable.dataType = ((VariableEntry) entry).getType();
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.array.accept(this);
        var arrayDataType = arrayAccess.array.dataType;

        if (!(arrayDataType instanceof ArrayType)) {
            error.indexingNonArray(arrayAccess);
            return;
        }

        arrayAccess.dataType = ((ArrayType) arrayDataType).getBaseType();

        arrayAccess.index.accept(this);
        var arrayIndexDataType = arrayAccess.index.dataType;
        if (arrayIndexDataType != target.intType) {
            error.expressionMismatchedType(arrayAccess.index, target.intType);
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.target.accept(this);
        assignStatement.value.accept(this);
        var targetDataType = assignStatement.target.dataType;
        var valueDataType = assignStatement.value.dataType;

        if (targetDataType != valueDataType) {
            error.expressionMismatchedType(assignStatement.value, targetDataType);
        }

        if (valueDataType != target.intType) {
            error.expressionMismatchedType(assignStatement.value, target.intType);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.condition.accept(this);
        var conditionDataType = ifStatement.condition.dataType;
        if (conditionDataType != target.boolType) {
            error.expressionMismatchedType(ifStatement.condition, target.boolType);
        }
        ifStatement.consequence.accept(this);
        ifStatement.alternative.accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.condition.accept(this);
        var conditionDataType = whileStatement.condition.dataType;
        if (conditionDataType != target.boolType) {
            error.expressionMismatchedType(whileStatement.condition, target.boolType);
        }
        whileStatement.body.accept(this);
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);
        variableExpression.dataType = variableExpression.variable.dataType;
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        for (var s : compoundStatement.statements) {
            s.accept(this);
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        var entry = symbolTable.lookup(callStatement.procedureName.symbol, ProcedureEntry.class,
                (candidate) -> error.procedureUndefined(callStatement, candidate));

        if (!(entry instanceof ProcedureEntry procEntry)) {
            error.callOfNonProcedure(callStatement);
            return;
        }

        if (callStatement.arguments.size() != procEntry.getParameterTypes().size()) {
            error.wrongNumberOfArguments(callStatement, procEntry.getParameterTypes().size());
        }

        for (var i = 0; i < callStatement.arguments.size(); i++) {
            var currentParameterType = procEntry.getParameterTypes().get(i);

            final Expression currentArgument = callStatement.arguments.get(i);
            currentArgument.accept(this);

            if (currentParameterType.isReference() && !(currentArgument instanceof VariableExpression)) {
                error.argumentMustBeAVariable(callStatement, i + 1);
            }

            if (currentArgument.dataType != currentParameterType.getType()) {
                error.argumentTypeMismatch(callStatement, i + 1, currentParameterType.getType(), currentArgument.dataType);
            }
        }
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        intLiteral.dataType = target.intType;
    }
}
