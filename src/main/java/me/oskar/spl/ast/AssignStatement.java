package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class AssignStatement extends Statement {

    public final Variable target;
    public final Expression value;

    public AssignStatement(Token.Position position, Variable target, Expression value) {
        super(position);
        this.target = target;
        this.value = value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("AssignStatement", target, value);
    }
}
