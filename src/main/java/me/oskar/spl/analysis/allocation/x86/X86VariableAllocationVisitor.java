package me.oskar.spl.analysis.allocation.x86;

import me.oskar.spl.ast.ProcedureDeclaration;
import me.oskar.spl.ast.Program;
import me.oskar.spl.ast.VariableDeclaration;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.table.VariableEntry;

public class X86VariableAllocationVisitor extends BaseVisitor {

    private final SymbolTable symbolTable;
    private final Target target;
    private final ProcedureEntry currentProcedure;

    protected X86VariableAllocationVisitor(SymbolTable symbolTable, Target target) {
        this.symbolTable = symbolTable;
        this.target = target;
        this.currentProcedure = null;
    }

    private X86VariableAllocationVisitor(SymbolTable symbolTable, Target target, ProcedureEntry currentProcedure) {
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

    public void visit(ProcedureDeclaration procedureDeclaration) {
        var procEntry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
        var variableAllocationVisitor = new X86VariableAllocationVisitor(procEntry.getLocalTable(), target, procEntry);
        var stackLayout = new X86StackLayout();
        procEntry.setStackLayout(stackLayout);

        for (var v : procedureDeclaration.variables) {
            v.accept(variableAllocationVisitor);
        }

        var callRegisters = new String[] { "rdi", "rsi", "rdx", "rcx", "r8", "r9" };

        for (var i = 0; i < procedureDeclaration.parameters.size(); i++) { // TODO: dont use indexes for parametertypes
            var variableEntry = (VariableEntry) procEntry.getLocalTable().lookup(procedureDeclaration.parameters.get(i).name);
            var parameterType = procEntry.getParameterTypes().get(i);

            if (i < 6) {
                parameterType.setPosition(callRegisters[i]);
                variableEntry.setPosition("[rbp+" + (-stackLayout.localVariableAreaSize -
                        (parameterType.isReference() ? target.wordSize : variableEntry.getType().getByteSize())) + "]");
                stackLayout.localVariableAreaSize += parameterType.isReference() ? target.wordSize : variableEntry.getType().getByteSize();
            } else {
                parameterType.setPosition("[rsp+" + stackLayout.argumentAreaSize + "]");
                variableEntry.setPosition("[rbp+" + (stackLayout.argumentAreaSize + 16) + "]");  // old frame pointer (8) + return address (8)
                stackLayout.argumentAreaSize += parameterType.isReference() ? target.wordSize : variableEntry.getType().getByteSize();
            }
        }

        var outgoingAreaVisitor = new OutgoingAreaSizeVisitor(symbolTable, target);
        procedureDeclaration.accept(outgoingAreaVisitor);
        stackLayout.outgoingAreaSize = outgoingAreaVisitor.maxSize;
    }

    @Override
    public void visit(VariableDeclaration variableDeclaration) {
        var entry = (VariableEntry) symbolTable.lookup(variableDeclaration.name);
        assert currentProcedure != null;
        var stackLayout = (X86StackLayout) currentProcedure.getStackLayout();

        entry.setInMemory(true);
        entry.setPosition("[rbp+" + (-stackLayout.localVariableAreaSize - entry.getType().getByteSize()) + "]");
        stackLayout.localVariableAreaSize += entry.getType().getByteSize();
    }
}
