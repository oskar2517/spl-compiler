package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class BinaryExpression extends Expression {

    public enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        EQUAL,
        NOT_EQUAL,
        LESS_THAN,
        LESS_THAN_EQUAL,
        GREATER_THAN,
        GREATER_THAN_EQUAL
    }

    public final Operator operator;
    public final Expression leftOperand;
    public final Expression rightOperand;

    public BinaryExpression(Span span, Operator operator, Expression leftOperand, Expression rightOperand) {
        super(span);

        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

