package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

import java.util.Objects;

public class IntLiteral extends Expression {

    public final int value;

    public IntLiteral(Token.Position position, int value) {
        super(position);

        this.value = value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("IntLiteral", value);
    }
}

