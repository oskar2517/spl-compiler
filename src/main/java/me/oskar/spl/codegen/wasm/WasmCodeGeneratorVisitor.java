package me.oskar.spl.codegen.wasm;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;

public class WasmCodeGeneratorVisitor extends BaseVisitor {

    private int labelIndex = 0;

    private SymbolTable symbolTable;
    private final CodePrinter output;

    protected WasmCodeGeneratorVisitor(SymbolTable symbolTable, CodePrinter output) {
        this.symbolTable = symbolTable;
        this.output = output;
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
    public void visit(AssignStatement assignStatement) {
        if (assignStatement.target instanceof ArrayAccess) {
            assignStatement.target.accept(this);
            assignStatement.value.accept(this);
            output.println("i32.store");
        } else if (assignStatement.target instanceof NamedVariable nv) {
            var variableEntry = (VariableEntry) symbolTable.lookup(nv.name.symbol);

            if (variableEntry.isReference() || variableEntry.isInMemory()) {
                output.println("local.get $%s", nv.name.symbol);
                assignStatement.value.accept(this);
                output.println("i32.store");
            } else {
                assignStatement.value.accept(this);
                output.println("local.set $%s", nv.name.symbol);
            }
        }
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);

        if (variableExpression.variable instanceof ArrayAccess) {
            output.println("i32.load");
        } else if (variableExpression.variable instanceof NamedVariable nv) {
            var variableEntry = (VariableEntry) symbolTable.lookup(nv.name.symbol);

            if (variableEntry.isInMemory() || variableEntry.isReference()) {
                output.println("i32.load");
            }
        }
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        output.println("local.get $%s", namedVariable.name.symbol);
    }

    @Override
    public void visit(CallStatement callStatement) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName.symbol);

        for (var i = 0; i < callStatement.arguments.size(); i++) {
            Expression argument = callStatement.arguments.get(i);

            if (procedureEntry.getParameterTypes().get(i).isReference()) {
                var variableExpression = (VariableExpression) argument;
                variableExpression.variable.accept(this);
            } else {
                argument.accept(this);
            }
        }

        output.println("call $%s", prefixIdent(callStatement.procedureName.symbol));
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.leftOperand.accept(this);
        binaryExpression.rightOperand.accept(this);

        switch (binaryExpression.operator) {
            case ADD -> output.println("i32.add");
            case SUBTRACT -> output.println("i32.sub");
            case MULTIPLY -> output.println("i32.mul");
            case DIVIDE -> output.println("i32.div_s");
            case EQUAL -> output.println("i32.eq");
            case NOT_EQUAL -> output.println("i32.ne");
            case GREATER_THAN -> output.println("i32.gt_s");
            case GREATER_THAN_EQUAL -> output.println("i32.ge_s");
            case LESS_THAN -> output.println("i32.lt_s");
            case LESS_THAN_EQUAL -> output.println("i32.le_s");
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (unaryExpression.operator == UnaryExpression.Operator.NEGATE) {
            output.println("i32.const 0");
            unaryExpression.operand.accept(this);
            output.println("i32.sub");
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
        ifStatement.condition.accept(this);
        output.println("(if");
        output.incIndentLevel();
        output.println("(then");
        output.incIndentLevel();
        ifStatement.consequence.accept(this);
        output.decIndentLevel();
        output.println(")");
        if (!(ifStatement.alternative instanceof EmptyStatement)) {
            output.println("(else");
            output.incIndentLevel();
            ifStatement.alternative.accept(this);
            output.decIndentLevel();
            output.println(")");
        }
        output.decIndentLevel();
        output.println(")");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var label = labelIndex++;

        output.println("(block $block%s", label);
        output.incIndentLevel();

        output.println("(loop $loop%s", label);
        output.incIndentLevel();

        whileStatement.condition.accept(this);
        output.println("i32.const 1");
        output.println("i32.ne");
        output.println("br_if $block%s", label);

        whileStatement.body.accept(this);

        output.println("br $loop%s", label);

        output.decIndentLevel();
        output.println(")");


        output.decIndentLevel();
        output.println(")");
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        output.println("i32.const %s", intLiteral.value);
    }

    @Override
    public void visit(ArrayAccess arrayAccess) { // TODO: bounds check, ershov
        var arrayType = (ArrayType) arrayAccess.array.dataType;

        arrayAccess.array.accept(this);
        arrayAccess.index.accept(this);
        output.println("i32.const %s", arrayType.getArraySize());
        output.println("call $checkArrayIndex");
        output.println("i32.const %s", arrayType.getBaseType().getByteSize());
        output.println("i32.mul");
        output.println("i32.add");
    }

    private void generateMemoryGrowth() {
        output.println(";; grow memory if required");
        output.println("i32.const 1"); // Default memory size is one page
        output.println("i32.const 0");
        output.println("i32.load");
        output.println("i32.const 65536"); // page size is 65,536
        output.println("i32.div_u");
        output.println("i32.add");
        output.println("memory.size");
        output.println("i32.sub");
        output.println("memory.grow");
        output.println("drop");
        output.println();
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name.symbol);
        var lastTable = symbolTable;

        output.printf("(func $%s ", prefixIdent(procedureDeclaration.name.symbol));
        for (var p : procedureDeclaration.parameters) {
            output.printf("(param $%s i32) ", p.name.symbol);
        }
        output.println();
        output.incIndentLevel();

        for (var v : procedureDeclaration.variables) {
            output.println("(local $%s i32)", v.name.symbol);
        }
        output.println();

        // Allocate local variable area
        if (procedureEntry.getStackLayout().getFrameSize() > 0) {
            output.println(";; allocate local variable area");
            output.println("i32.const 0");
            output.println("i32.const %s", procedureEntry.getStackLayout().getFrameSize());
            output.println("i32.const 0");
            output.println("i32.load");
            output.println("i32.add");
            output.println("i32.store");
            output.println();
        }

        generateMemoryGrowth();

        symbolTable = procedureEntry.getLocalTable();

        for (var v : procedureDeclaration.variables) {
            var variableEntry = (VariableEntry) symbolTable.lookup(v.name.symbol);

            if (variableEntry.isInMemory()) {
                output.println(";; assigning memory for local variable %s", v.name.symbol);
                output.println("i32.const 0");
                output.println("i32.load");
                output.println("i32.const %s", variableEntry.getPosition().getRealPosition());
                output.println("i32.add");
                output.println("local.set $%s", v.name.symbol);
                output.println();
            }
        }

        for (var p : procedureDeclaration.parameters) {
            var variableEntry = (VariableEntry) symbolTable.lookup(p.name.symbol);

            if (variableEntry.isInMemory()) {
                output.println(";; assigning memory for parameter %s", p.name.symbol);
                output.println("i32.const 0");
                output.println("i32.load");
                output.println("i32.const %s", variableEntry.getPosition().getRealPosition());
                output.println("i32.add");
                output.println("local.get $%s", p.name.symbol);
                output.println("i32.store");

                output.println("i32.const 0");
                output.println("i32.load");
                output.println("i32.const %s", variableEntry.getPosition().getRealPosition());
                output.println("i32.add");
                output.println("local.set $%s", p.name.symbol);
                output.println();
            }
        }

        for (var s : procedureDeclaration.body) {
            s.accept(this);
        }

        symbolTable = lastTable;

        // Subtract local variable area
        if (procedureEntry.getStackLayout().getFrameSize() > 0) {
            output.println();
            output.println(";; deallocate local variable area");
            output.println("i32.const 0");
            output.println("i32.const 0");
            output.println("i32.load");
            output.println("i32.const %s", procedureEntry.getStackLayout().getFrameSize());
            output.println("i32.sub");
            output.println("i32.store");
        }

        output.decIndentLevel();
        output.println(")");
        output.println();
    }
}
