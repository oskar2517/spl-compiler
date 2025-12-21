package me.oskar.spl.codegen.llvm;

import me.oskar.spl.Target;
import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;

import java.util.ArrayList;

class LLVMCodeGeneratorVisitor extends BaseVisitor {

    private SymbolTable symbolTable;
    private final CodePrinter output;
    private final Target target;

    private EnumeratedRegister currentEnumeratedRegister = new EnumeratedRegister(-1);
    private Label currentLabel = new Label(-1);

    protected LLVMCodeGeneratorVisitor(SymbolTable symbolTable, CodePrinter output, Target target) {
        this.symbolTable = symbolTable;
        this.output = output;
        this.target = target;
    }

    private Label nextLabel() {
        currentLabel = currentLabel.next();
        return currentLabel;
    }

    private void nextRegister() {
        currentEnumeratedRegister = currentEnumeratedRegister.next();
    }

    private void generateCondition(Expression condition, Label thenLabel, Label elseLabel) {
        var binaryExpression = (BinaryExpression) condition;
        binaryExpression.leftOperand.accept(this);
        var leftRegister = currentEnumeratedRegister;

        binaryExpression.rightOperand.accept(this);
        var rightRegister = currentEnumeratedRegister;

        nextRegister();

        switch (binaryExpression.operator) {
            case EQUAL -> output.println("%s = icmp eq i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case NOT_EQUAL -> output.println("%s = icmp ne i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case LESS_THAN -> output.println("%s = icmp slt i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case LESS_THAN_EQUAL -> output.println("%s = icmp sle i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case GREATER_THAN -> output.println("%s = icmp sgt i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case GREATER_THAN_EQUAL -> output.println("%s = icmp sge i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
        }

        output.println("br i1 %s, label %%%s, label %%%s", currentEnumeratedRegister, thenLabel, elseLabel);
    }

    @Override
    public void visit(Program program) {
        for (var gd : program.declarations) {
            gd.accept(this);
        }
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        for (var s : compoundStatement.statements) {
            s.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        var thenLabel = nextLabel();
        var elseLabel = nextLabel();
        var mergeLabel = nextLabel();

        generateCondition(ifStatement.condition, thenLabel, elseLabel);

        output.decIndentLevel();
        output.println(thenLabel + ":");
        output.incIndentLevel();
        ifStatement.consequence.accept(this);
        output.println("br label %%" + mergeLabel);

        output.decIndentLevel();

        output.println(elseLabel + ":");
        output.incIndentLevel();
        ifStatement.alternative.accept(this);
        output.println("br label %%" + mergeLabel);

        output.decIndentLevel();

        output.println(mergeLabel + ":");
        output.incIndentLevel();
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var condLabel = nextLabel();
        var bodyLabel = nextLabel();
        var endLabel = nextLabel();

        output.println("br label %%" + condLabel);

        output.decIndentLevel();
        output.println(condLabel + ":");
        output.incIndentLevel();
        generateCondition(whileStatement.condition, bodyLabel, endLabel);

        output.decIndentLevel();

        output.println(bodyLabel + ":");
        output.incIndentLevel();
        whileStatement.body.accept(this);
        output.println("br label %%" + condLabel);

        output.decIndentLevel();

        output.println(endLabel + ":");
        output.incIndentLevel();

    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.target.accept(this);
        var addressRegister = currentEnumeratedRegister;

        assignStatement.value.accept(this);
        var valueRegister = currentEnumeratedRegister;

        output.println("store i64 %s, i64* %s, align %s", valueRegister, addressRegister, target.wordSize);
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        var variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name.symbol);

        nextRegister();
        if (variableEntry.isReference()) {
            output.println("%s = load i64*, i64* %s", currentEnumeratedRegister, new Register(namedVariable.name.symbol).address());
        } else {
            output.println("%s = getelementptr i64, i64* %s, i64 0", currentEnumeratedRegister, new Register(namedVariable.name.symbol).address());
        }
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);
        var variableExpressionRegister = currentEnumeratedRegister;

        nextRegister();
        output.println("%s = load i64, i64* %s", currentEnumeratedRegister, variableExpressionRegister);
    }

    @Override
    public void visit(CallStatement callStatement) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName.symbol);

        var args = new ArrayList<String>();

        for (int i = 0; i < callStatement.arguments.size(); i++) {
            var argument = callStatement.arguments.get(i);
            var parameter = procedureEntry.getParameterTypes().get(i);

            if (parameter.isReference()) {
                var variableExpression = (VariableExpression) argument;
                variableExpression.variable.accept(this);
                args.add("i64* " + currentEnumeratedRegister);
            } else {
                argument.accept(this);
                args.add("i64 " + currentEnumeratedRegister);
            }
        }
        output.println("call void %s(%s)", new Global(callStatement.procedureName.symbol), String.join(", ", args));
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        nextRegister();
        output.println("%s = add i64 0, %s", currentEnumeratedRegister, intLiteral.value);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.leftOperand.accept(this);
        var leftRegister = currentEnumeratedRegister;

        binaryExpression.rightOperand.accept(this);
        var rightRegister = currentEnumeratedRegister;

        nextRegister();

        switch (binaryExpression.operator) {
            case ADD -> output.println("%s = add i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case SUBTRACT -> output.println("%s = sub i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case MULTIPLY -> output.println("%s = mul i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
            case DIVIDE -> output.println("%s = sdiv i64 %s, %s", currentEnumeratedRegister, leftRegister, rightRegister);
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        var arrayType = (ArrayType) arrayAccess.array.dataType;

        arrayAccess.array.accept(this);
        var arrayRegister = currentEnumeratedRegister;

        arrayAccess.index.accept(this);
        var indexRegister = currentEnumeratedRegister;

        var convertedType = LLVMCodeGenerator.convertTypeToLLVM(target, arrayType);
        nextRegister();


        output.println("%s = getelementptr %s, %s* %s, i64 0, i64 %s", currentEnumeratedRegister, convertedType, convertedType, arrayRegister, indexRegister);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.operand.accept(this);
        var operandRegister = currentEnumeratedRegister;

        nextRegister();
        if (unaryExpression.operator == UnaryExpression.Operator.NEGATE) {
            output.println("%s = sub i64 0, %s", currentEnumeratedRegister, operandRegister);
        } else {
            throw new IllegalStateException("Unsupported unary operator " + unaryExpression.operator);
        }
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name.symbol);
        var lastTable = symbolTable;
        symbolTable = procedureEntry.getLocalTable();

        var parameters = new ArrayList<String>();
        for (var i = 0; i < procedureEntry.getParameterTypes().size(); i++) {
            var paramDecl = procedureDeclaration.parameters.get(i);
            var paramType = procedureEntry.getParameterTypes().get(i);

            parameters.add("%s %s".formatted(LLVMCodeGenerator.mapParameterTypeToLLVMType(target, paramType),
                    new Register(paramDecl.name.symbol)));
        }

        output.println("define void %s(%s) {", new Global(procedureDeclaration.name.symbol),
                String.join(", ", parameters));
        output.println("entry:");
        output.incIndentLevel();
        for (var i = 0; i < procedureEntry.getParameterTypes().size(); i++) {
            var paramDecl = procedureDeclaration.parameters.get(i);
            var paramType = procedureEntry.getParameterTypes().get(i);

            var register = new Register(paramDecl.name.symbol);
            var address = register.address();
            var type = LLVMCodeGenerator.mapParameterTypeToLLVMType(target, paramType);

            output.println("%s = alloca %s, align %s", address, type, target.wordSize);
            output.println("store %s %s, %s* %s, align %s", type, register, type, address, target.wordSize);
        }

        for (var v : procedureDeclaration.variables) {
            var varEntry = (VariableEntry) symbolTable.lookup(v.name.symbol);

            var register = new Register(v.name.symbol).address();
            var type = LLVMCodeGenerator.convertTypeToLLVM(target, varEntry.getType());

            output.println("%s = alloca %s, align %s", register, type, target.wordSize);
        }

        output.println();

        for (var s : procedureDeclaration.body) {
            s.accept(this);
        }

        output.println();

        output.println("ret void");

        output.decIndentLevel();

        output.println("}");
        output.println();

        symbolTable = lastTable;
    }
}
