package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class TypeDeclaration extends GlobalDeclaration {

    public final TypeExpression typeExpression;

    public TypeDeclaration(Span span, Identifier name, TypeExpression typeExpression) {
        super(span, name);

        this.typeExpression = typeExpression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

