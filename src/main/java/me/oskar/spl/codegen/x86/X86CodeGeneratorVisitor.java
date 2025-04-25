package me.oskar.spl.codegen.x86;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;

public class X86CodeGeneratorVisitor extends BaseVisitor {

    private Register currentRegister = new Register(-1);
    private Label currentLabel = new Label(-1);
    private SymbolTable symbolTable;
    private final CodePrinter output;

    protected X86CodeGeneratorVisitor(SymbolTable symbolTable, CodePrinter output) {
        this.symbolTable = symbolTable;
        this.output = output;
    }

    private void nextRegister() {
        currentRegister = currentRegister.next();
    }

    private void previousRegister() {
        currentRegister = currentRegister.previous();
    }

    private Label nextLabel() {
        currentLabel = currentLabel.next();
        return currentLabel;
    }

    private void generateCondition(Expression condition, Label label) {
        var binaryExpression = (BinaryExpression) condition;
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);
        output.println("cmp %s, %s", currentRegister.previous(), currentRegister);

        switch (binaryExpression.operator) {
            case EQUAL -> output.println("jne %s", label);
            case NOT_EQUAL -> output.println("je %s", label);
            case LESS_THAN -> output.println("jge %s", label);
            case LESS_THAN_EQUAL -> output.println("jg %s", label);
            case GREATER_THAN -> output.println("jle %s", label);
            case GREATER_THAN_EQUAL -> output.println("jl %s", label);
        }

        previousRegister();
        previousRegister();
    }

    private String prefixIdent(String ident) {
        return String.format("_%s", ident);
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
    public void visit(AssignStatement assignStatement) {
        assignStatement.target.accept(this);
        var addressRegister = currentRegister;

        assignStatement.value.accept(this);
        var valueRegister = currentRegister;

        previousRegister();

        output.println("mov [%s], %s", addressRegister, valueRegister);
        previousRegister();
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        nextRegister();
        output.println("mov %s, qword %s", currentRegister, intLiteral.value);
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        var variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name.symbol);

        nextRegister();
        if (variableEntry.isReference()) {
            output.println(String.format("mov %s, %s", currentRegister, variableEntry.getPosition().getRealPosition()));
        } else {
            output.println(String.format("lea %s, %s", currentRegister, variableEntry.getPosition().getRealPosition()));
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        var label0 = nextLabel();

        generateCondition(ifStatement.condition, label0);

        if (ifStatement.alternative instanceof EmptyStatement) {
            ifStatement.consequence.accept(this);

            output.println("%s:", label0);
        } else {
            ifStatement.consequence.accept(this);
            var label1 = nextLabel();
            output.println("jmp %s", label1);

            output.println("%s:", label0);
            ifStatement.alternative.accept(this);
            output.println("%s:", label1);
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var label0 = nextLabel();
        var label1 = nextLabel();

        output.println("%s:", label0);
        generateCondition(whileStatement.condition, label1);

        whileStatement.body.accept(this);

        output.println("jmp %s", label0);
        output.println("%s:", label1);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        var arrayType = (ArrayType) arrayAccess.array.dataType;

        arrayAccess.array.accept(this);
        arrayAccess.index.accept(this);

        output.println("cmp %s, 0", currentRegister);
        output.println("jl __arrayAccessOutOfBounds");
        output.println("cmp %s, %s", currentRegister, arrayType.getArraySize());
        output.println("jae __arrayAccessOutOfBounds");

        output.println("imul %s, %s", currentRegister, arrayType.getBaseType().getByteSize());
        output.println("add %s, %s", currentRegister.previous(), currentRegister);

        previousRegister();
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (unaryExpression.operator == UnaryExpression.Operator.NEGATE) {
            unaryExpression.operand.accept(this);
            output.println("neg %s", currentRegister);
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName.symbol);

        var argumentsReversed = callStatement.arguments.reversed();
        var parameterTypesReversed = procedureEntry.getParameterTypes().reversed();

        for (int i = 0; i < callStatement.arguments.size(); i++) {
            var argument = argumentsReversed.get(i);
            var parameter = parameterTypesReversed.get(i);

            if (parameter.isReference()) {
                var variableExpression = (VariableExpression) argument;
                variableExpression.variable.accept(this);
            } else {
                argument.accept(this);
            }

            output.println("mov %s, %s", parameter.getPosition().getRealPosition(), currentRegister);
            previousRegister();
        }
        output.println("call %s", prefixIdent(callStatement.procedureName.symbol));
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);
        output.println("mov %s, [%s]", currentRegister, currentRegister);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);

        switch (binaryExpression.operator) {
            case ADD -> output.println("add %s, %s", currentRegister.previous(), currentRegister);
            case SUBTRACT -> output.println("sub %s, %s", currentRegister.previous(), currentRegister);
            case MULTIPLY -> output.println("imul %s, %s", currentRegister.previous(), currentRegister);
            case DIVIDE -> {
                output.println("mov rax, %s", currentRegister.previous());
                output.println("cqo");
                output.println("idiv %s", currentRegister);
                output.println("mov %s, rax", currentRegister.previous());
            }
        }

        previousRegister();
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name.symbol);
        var lastTable = symbolTable;

        output.println("%s:", prefixIdent(procedureDeclaration.name.symbol));
        output.incIndentLevel();
        output.println("; Allocate stack frame");
        output.println("push rbp");
        output.println("mov rbp, rsp");
        output.println("sub rsp, %s", procedureEntry.getStackLayout().getFrameSize());
        output.println();

        output.println("; Backup callee-saved registers");
        output.println("push rbx");
        output.println("push r12");
        output.println("push r13");
        output.println("push r14");
        output.println("push r15");
        output.println("sub rsp, 8 ; Align stack");
        output.println();

        symbolTable = procedureEntry.getLocalTable();

        for (var i = 0; i < 6; i++) {
            if (i >= procedureEntry.getParameterTypes().size()) break;

            var parameter = procedureEntry.getParameterTypes().get(i);
            var variableEntry = (VariableEntry) symbolTable.lookup(procedureDeclaration.parameters.get(i).name.symbol);

            output.println("mov %s, %s", variableEntry.getPosition().getRealPosition(),
                    parameter.getPosition().getRealPosition());
        }

        for (var s : procedureDeclaration.body) {
            s.accept(this);
        }

        symbolTable = lastTable;

        output.println();
        output.println("; Restore callee-saved registers");
        output.println("pop r15");
        output.println("pop r14");
        output.println("pop r13");
        output.println("pop r12");
        output.println("pop rbx");
        output.println("add rsp, 8 ; Align stack");

        output.println();
        output.println("; Destroy stack frame");
        output.println("mov rsp, rbp");
        output.println("pop rbp");
        output.println("ret");
        output.decIndentLevel();
        output.println();
    }
}
