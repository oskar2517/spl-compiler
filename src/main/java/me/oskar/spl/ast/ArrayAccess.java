package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class ArrayAccess extends Variable {

    public final Variable array;
    public final Expression index;

    public ArrayAccess(Token.Position position, Variable array, Expression index) {
        super(position);

        this.array = array;
        this.index = index;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("ArrayAccess", array, index);
    }
}

