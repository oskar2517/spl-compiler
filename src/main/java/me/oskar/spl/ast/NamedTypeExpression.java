package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class NamedTypeExpression extends TypeExpression {

    public final Identifier name;

    public NamedTypeExpression(Span span, Identifier name) {
        super(span);

        this.name = name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("NamedTypeExpression", name);
    }
}

