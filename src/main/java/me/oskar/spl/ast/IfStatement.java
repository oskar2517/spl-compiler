package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class IfStatement extends Statement {

    public final Expression condition;
    public final Statement consequence;
    public final Statement alternative;

    public IfStatement(Span span, Expression condition, Statement consequence, Statement alternative) {
        super(span);

        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

