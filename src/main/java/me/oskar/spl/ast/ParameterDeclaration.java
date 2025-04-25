package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class ParameterDeclaration extends Node {

    public final Identifier name;
    public final TypeExpression typeExpression;
    public final boolean isReference;

    public ParameterDeclaration(Span span, Identifier name, TypeExpression typeExpression, boolean isReference) {
        super(span);

        this.name = name;
        this.typeExpression = typeExpression;
        this.isReference = isReference;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("ParameterDeclaration", name, typeExpression, isReference);
    }
}

