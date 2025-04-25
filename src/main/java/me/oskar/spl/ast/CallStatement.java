package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

import java.util.List;

public class CallStatement extends Statement {

    public final Identifier procedureName;
    public final List<Expression> arguments;

    public CallStatement(Span span, Identifier procedureName, List<Expression> arguments) {
        super(span);

        this.procedureName = procedureName;
        this.arguments = arguments;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("CallStatement", procedureName, formatAst("Arguments", arguments.toArray()));
    }
}
