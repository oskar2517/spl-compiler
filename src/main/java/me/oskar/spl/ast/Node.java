package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitable;
import me.oskar.spl.lexer.Token;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Node implements Visitable {

    public final Token.Position position;

    public Node(Token.Position position) {
        this.position = position;
    }

    static String formatAst(String name, Object... arguments) {
        return formatAst(name, Arrays.stream(arguments).map(o -> o == null ? "NULL" : o.toString()).collect(Collectors.toList()));
    }

    private static String formatAst(String name, List<String> arguments) {
        if (arguments.isEmpty()) {
            return String.format("%s()", name);
        } else {
            return String.format("%s(\n%s)",
                    name,
                    indent(String.join(",\n", arguments)).stripTrailing());
        }
    }

    private static String indent(String str) {
        return str.lines()
                .map(s -> " ".repeat(2) + s)
                .collect(Collectors.joining("\n"));
    }
}