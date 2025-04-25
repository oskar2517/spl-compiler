package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.Target;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;

public class OutgoingAreaSizeVisitor extends BaseVisitor {

    public int maxSize = 0;
    private final SymbolTable symbolTable;
    private final Target target;

    protected OutgoingAreaSizeVisitor(SymbolTable symbolTable, Target target) {
        this.symbolTable = symbolTable;
        this.target = target;
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        for (var s : procedureDeclaration.body) {
            s.accept(this);
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.body.accept(this);
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.consequence.accept(this);
        ifStatement.alternative.accept(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        for (var s : compoundStatement.statements) {
            s.accept(this);
        }
    }

    @Override
    public void visit(CallStatement callStatement) {
        var procedureEntry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName.symbol);

        var argumentsSize = 0;

        for (var i = 6; i < procedureEntry.getParameterTypes().size(); i++) {
            var p = procedureEntry.getParameterTypes().get(i);
            argumentsSize += p.isReference() ? target.wordSize : p.getType().getByteSize();
        }

        if (argumentsSize > maxSize) {
            maxSize = argumentsSize;
        }
    }
}
