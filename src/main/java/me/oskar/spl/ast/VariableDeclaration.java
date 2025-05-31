package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class VariableDeclaration extends Node {

    public final Identifier name;
    public final TypeExpression typeExpression;

    public VariableDeclaration(Span span, Identifier name, TypeExpression typeExpression) {
        super(span);

        this.name = name;
        this.typeExpression = typeExpression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

