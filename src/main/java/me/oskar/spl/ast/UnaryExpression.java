package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class UnaryExpression extends Expression {

    public enum Operator {
        NEGATE
    }

    public final Operator operator;
    public final Expression operand;

    public UnaryExpression(Token.Position position, Operator operator, Expression operand) {
        super(position);

        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("UnaryExpression", operator, operand);
    }
}
