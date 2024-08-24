package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.lexer.Token;

import java.util.List;

public class CompoundStatement extends Statement {

    public final List<Statement> statements;

    public CompoundStatement(Token.Position position, List<Statement> statements) {
        super(position);
        this.statements = statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatAst("CompoundStatement", statements.toArray());
    }
}

