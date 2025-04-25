package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class Identifier extends Node {

    public final String symbol;

    public Identifier(Span span, String symbol) {
        super(span);

        this.symbol = symbol;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("Identifier", symbol);
    }
}
