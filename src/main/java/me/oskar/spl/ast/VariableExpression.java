package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class VariableExpression extends Expression {

    public final Variable variable;

    public VariableExpression(Token.Position position, Variable variable) {
        super(position);

        this.variable = variable;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("VariableExpression", variable);
    }
}

