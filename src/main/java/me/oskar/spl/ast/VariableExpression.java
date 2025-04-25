package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class VariableExpression extends Expression {

    public final Variable variable;

    public VariableExpression(Span span, Variable variable) {
        super(span);

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

