package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

public class BinaryExpression extends Expression {

    public enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        EQUAL,
        NOT_EQUAL,
        LESS_THEN,
        LESS_THEN_EQUAL,
        GREATER_THEN,
        GREATER_THEN_EQUAL
    }

    public final Operator operator;
    public final Expression leftOperand;
    public final Expression rightOperand;

    public BinaryExpression(Token.Position position, Operator operator, Expression leftOperand, Expression rightOperand) {
        super(position);

        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("BinaryExpression", operator, leftOperand, rightOperand);
    }
}

