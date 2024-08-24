package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class NamedVariable extends Variable {

    public final String name;

    public NamedVariable(Token.Position position, String name) {
        super(position);

        this.name = name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("NamedVariable", name);
    }
}

