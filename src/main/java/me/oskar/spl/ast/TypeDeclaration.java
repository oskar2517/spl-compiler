package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class TypeDeclaration extends GlobalDeclaration {

    public final TypeExpression typeExpression;

    public TypeDeclaration(Token.Position position, String name, TypeExpression typeExpression) {
        super(position, name);

        this.typeExpression = typeExpression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("TypeDeclaration", name, typeExpression);
    }
}

