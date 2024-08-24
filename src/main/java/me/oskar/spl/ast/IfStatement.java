package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class IfStatement extends Statement {

    public final Expression condition;
    public final Statement consequence;
    public final Statement alternative;

    public IfStatement(Token.Position position, Expression condition, Statement consequence, Statement alternative) {
        super(position);

        this.condition = condition;
        this.consequence = consequence;
        this.alternative = alternative;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("IfStatement", condition, consequence, alternative);
    }
}

