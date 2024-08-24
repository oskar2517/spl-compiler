package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class ArrayTypeExpression extends TypeExpression {

    public final TypeExpression baseType;
    public final int arraySize;

    public ArrayTypeExpression(Token.Position position, TypeExpression baseType, int arraySize) {
        super(position);

        this.baseType = baseType;
        this.arraySize = arraySize;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("ArrayTypeExpression", baseType, arraySize);
    }
}

