package me.oskar.spl.parser;

import me.oskar.spl.lexer.TokenType;

import java.util.*;

public class AnchorSet {

    private final Set<TokenType> anchorSet;

    private AnchorSet(Set<TokenType> anchorSet) {
        this.anchorSet = anchorSet;
    }

    public static AnchorSet of(TokenType... types) {
        return new AnchorSet(new HashSet<>(Arrays.asList(types)));
    }

    public AnchorSet union(AnchorSet other) {
        var newSet = new HashSet<TokenType>();
        newSet.addAll(other.anchorSet);
        newSet.addAll(anchorSet);

        return new AnchorSet(newSet);
    }

    public AnchorSet add(TokenType... types) {
        return union(of(types));
    }

    public boolean contains(TokenType t) {
        return anchorSet.contains(t);
    }

    @Override
    public String toString() {
        return Arrays.toString(anchorSet.toArray());
    }
}
