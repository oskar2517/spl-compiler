package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class WhileStatement extends Statement {

    public final Expression condition;
    public final Statement body;

    public WhileStatement(Token.Position position, Expression condition, Statement body) {
        super(position);

        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("WhileStatement", condition, body);
    }
}

