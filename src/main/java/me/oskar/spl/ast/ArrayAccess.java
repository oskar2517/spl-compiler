package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class ArrayAccess extends Variable {

    public final Variable array;
    public final Expression index;

    public ArrayAccess(Span span, Variable array, Expression index) {
        super(span);

        this.array = array;
        this.index = index;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

