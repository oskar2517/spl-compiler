package me.oskar.spl.ast.invalid;

import me.oskar.spl.ast.GlobalDeclaration;
import me.oskar.spl.ast.visitor.Visitor;
import me.oskar.spl.position.Span;

public class InvalidGlobalDeclaration  extends GlobalDeclaration {

    public InvalidGlobalDeclaration(Span span) {
        super(span, null);
    }

    public void accept(Visitor visitor) {
        throw new IllegalStateException("Attempted to visit invalid node");
    }
}
