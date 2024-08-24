package me.oskar.spl.analysis.name;

import me.oskar.spl.ast.*;
import me.oskar.spl.ast.visitor.BaseVisitor;
import me.oskar.spl.error.Error;
import me.oskar.spl.table.*;
import me.oskar.spl.type.ArrayType;

import java.util.ArrayList;

public class NameAnalysisVisitor extends BaseVisitor {

    private final SymbolTable currentTable;
    private final boolean showTables;
    private final Error error;

    public NameAnalysisVisitor(SymbolTable currentTable, boolean showTables, Error error) {
        this.currentTable = currentTable;
        this.showTables = showTables;
        this.error = error;
    }

    @Override
    public void visit(Program program) {
        for (var decl : program.declarations) {
            decl.accept(this);
        }

        var mainProcedure = currentTable.lookup("main", error::mainMissing);
        if (!(mainProcedure instanceof ProcedureEntry)) {
            error.mainNotAProcedure();
            return;
        }
        if (!((ProcedureEntry) mainProcedure).getParameterTypes().isEmpty()) {
            error.mainMustNotHaveParameters();
        }
    }

    @Override
    public void visit(TypeDeclaration typeDeclaration) {
        typeDeclaration.typeExpression.accept(this);
        currentTable.enter(typeDeclaration.name, new TypeEntry(typeDeclaration.typeExpression.dataType),
                () -> error.redeclarationAsType(typeDeclaration));
    }

    @Override
    public void visit(NamedTypeExpression namedTypeExpression) {
        var typeEntry = currentTable.lookup(namedTypeExpression.name, TypeEntry.class,
                (candidate) -> error.typeUndefined(namedTypeExpression, candidate));

        if (!(typeEntry instanceof TypeEntry)) {
            error.notAType(namedTypeExpression);
            return;
        }

        namedTypeExpression.dataType = ((TypeEntry) typeEntry).getType();
    }

    @Override
    public void visit(ArrayTypeExpression arrayTypeExpression) {
        arrayTypeExpression.baseType.accept(this);
        var baseType = arrayTypeExpression.baseType.dataType;
        arrayTypeExpression.dataType = new ArrayType(baseType, arrayTypeExpression.arraySize);
    }

    @Override
    public void visit(ParameterDeclaration parameterDeclaration) {
        parameterDeclaration.typeExpression.accept(this);
        if (parameterDeclaration.typeExpression.dataType instanceof ArrayType && !parameterDeclaration.isReference) {
            error.mustBeReferenceParameter(parameterDeclaration);
        }
        var dataType = parameterDeclaration.typeExpression.dataType;
        currentTable.enter(parameterDeclaration.name, new VariableEntry(dataType, parameterDeclaration.isReference),
                () -> error.redeclarationAsParameter(parameterDeclaration));
    }

    @Override
    public void visit(VariableDeclaration variableDeclaration) {
        variableDeclaration.typeExpression.accept(this);
        var dataType = variableDeclaration.typeExpression.dataType;
        currentTable.enter(variableDeclaration.name, new VariableEntry(dataType, false),
                () -> error.redeclarationAsVariable(variableDeclaration));
    }

    @Override
    public void visit(ProcedureDeclaration procedureDeclaration) {
        var localTable = new SymbolTable(currentTable);
        var localNameAnalysisVisitor = new NameAnalysisVisitor(localTable, showTables, error);

        var parameterTypes = new ArrayList<ParameterType>();
        for (var pd : procedureDeclaration.parameters) {
            pd.accept(localNameAnalysisVisitor);
            parameterTypes.add(new ParameterType(pd.typeExpression.dataType, pd.isReference));
        }

        for (var vd : procedureDeclaration.variables) {
            vd.accept(localNameAnalysisVisitor);
        }

        var procedureEntry = new ProcedureEntry(localTable, parameterTypes);

        currentTable.enter(procedureDeclaration.name, procedureEntry,
                () -> error.redeclarationAsProcedure(procedureDeclaration));

        if (showTables) {
            NameAnalysis.printSymbolTableAtEndOfProcedure(procedureDeclaration.name, procedureEntry);
        }
    }
}
