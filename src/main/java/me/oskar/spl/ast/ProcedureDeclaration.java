package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

import java.util.List;

public class ProcedureDeclaration extends GlobalDeclaration {

    public final List<ParameterDeclaration> parameters;
    public final List<VariableDeclaration> variables;
    public final List<Statement> body;

    public ProcedureDeclaration(Span span, Identifier name, List<ParameterDeclaration> parameters,
                                List<VariableDeclaration> variables, List<Statement> body) {
        super(span, name);

        this.parameters = parameters;
        this.variables = variables;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
