package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

import java.util.List;

public class ProcedureDeclaration extends GlobalDeclaration {

    public final List<ParameterDeclaration> parameters;
    public final List<VariableDeclaration> variables;
    public final List<Statement> body;

    public ProcedureDeclaration(Token.Position position, String name, List<ParameterDeclaration> parameters, List<VariableDeclaration> variables, List<Statement> body) {
        super(position, name);
        this.parameters = parameters;
        this.variables = variables;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("ProcedureDeclaration",
                name,
                formatAst("Parameters", parameters.toArray()),
                formatAst("Variables", variables.toArray()),
                formatAst("Body", body.toArray())
        );
    }
}
