package me.oskar.spl.analysis.allocation.wasm;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.Target;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;
import me.oskar.spl.type.ArrayType;

public class WasmVariableAllocationVisitor extends BaseVisitor {

    private final SymbolTable symbolTable;
    private final Target target;
    private final ProcedureEntry currentProcedure;

    protected WasmVariableAllocationVisitor(SymbolTable symbolTable, Target target) {
        this.symbolTable = symbolTable;
        this.target = target;
        this.currentProcedure = null;
    }

    private WasmVariableAllocationVisitor(SymbolTable symbolTable, Target target, ProcedureEntry currentProcedure) {
        this.symbolTable = symbolTable;
        this.target = target;
        this.currentProcedure = currentProcedure;
    }

    @Override
    public void visit(Program program) {
        for (var decl : program.declarations) {
            decl.accept(this);
        }
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        procEntry.setStackLayout(new WasmStackLayout());
        var variableAllocationVisitor = new WasmVariableAllocationVisitor(procEntry.getLocalTable(), target, procEntry);

        for (var v : procedureDeclaration.variables) {
            v.accept(variableAllocationVisitor);
        }

        for (var s : procedureDeclaration.body) {
            s.accept(variableAllocationVisitor);
        }
    }

    @Override
    public void visit(VariableDeclaration variableDeclaration) {
        var entry = (VariableEntry) symbolTable.lookup(variableDeclaration.name);
        assert currentProcedure != null;
        var stackLayout = (WasmStackLayout) currentProcedure.getStackLayout();

        if (entry.getType() instanceof ArrayType) {
            entry.setInMemory(true);
            var variableOffset = -stackLayout.localVariableAreaSize - entry.getType().getByteSize();
            entry.setPosition(new LinearMemoryPosition(variableOffset));
            stackLayout.localVariableAreaSize += entry.getType().getByteSize();
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName);

        for (var i = 0; i < callStatement.arguments.size(); i++) {
            var currentArgument = callStatement.arguments.get(i);
            var currentParameterType = procedureEntry.getParameterTypes().get(i);

            if (currentParameterType.isReference()) {
                currentArgument.accept(this);
            }
        }
    }

    @Override
    public void visit(NamedVariable namedVariable) {
        var variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name);
        assert currentProcedure != null;
        var stackLayout = (WasmStackLayout) currentProcedure.getStackLayout();

        if (!variableEntry.isInMemory() && !variableEntry.isReference()) {
            variableEntry.setInMemory(true);
            var variableOffset = -stackLayout.localVariableAreaSize - namedVariable.dataType.getByteSize();
            variableEntry.setPosition(new LinearMemoryPosition(variableOffset));
            stackLayout.localVariableAreaSize += namedVariable.dataType.getByteSize();
        }
    }

    @Override
    public void visit(ArrayAccess arrayAccess) {
        arrayAccess.array.accept(this);
    }

    @Override
    public void visit(VariableExpression variableExpression) {
        variableExpression.variable.accept(this);
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.consequence.accept(this);
        ifStatement.alternative.accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.body.accept(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        for (Statement s : compoundStatement.statements) {
            s.accept(this);
        }
    }
}
