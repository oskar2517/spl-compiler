package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class NamedTypeExpression extends TypeExpression {

    public final String name;

    public NamedTypeExpression(Token.Position position, String name) {
        super(position);

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

