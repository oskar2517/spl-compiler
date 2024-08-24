package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class VariableDeclaration extends Node {

    public final String name;
    public final TypeExpression typeExpression;

    public VariableDeclaration(Token.Position position, String name, TypeExpression typeExpression) {
        super(position);

        this.name = name;
        this.typeExpression = typeExpression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("VariableDeclaration", name, typeExpression);
    }
}

