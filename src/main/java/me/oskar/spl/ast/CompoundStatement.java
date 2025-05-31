package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

import java.util.List;

public class CompoundStatement extends Statement {

    public final List<Statement> statements;

    public CompoundStatement(Span span, List<Statement> statements) {
        super(span);

        this.statements = statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

