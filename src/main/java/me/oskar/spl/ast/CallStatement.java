package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

import java.util.List;

public class CallStatement extends Statement {

    public final String procedureName;
    public final List<Expression> arguments;

    public CallStatement(Token.Position position, String procedureName, List<Expression> arguments) {
        super(position);

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
