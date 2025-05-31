package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class AssignStatement extends Statement {

    public final Variable target;
    public final Expression value;

    public AssignStatement(Span span, Variable target, Expression value) {
        super(span);

        this.target = target;
        this.value = value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
