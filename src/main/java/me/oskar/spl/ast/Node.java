package me.oskar.spl.ast;

import me.oskar.spl.ast.visitor.Visitable;
import me.oskar.spl.position.Span;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Node implements Visitable {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NoProperty {
    }

    private final Span span;

    public Node(Span span) {
        this.span = span;
    }

    @Override
    public String toString() {
        var fields = new ArrayList<>();

        for (var field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (!field.isAnnotationPresent(NoProperty.class)) {
                try {
                    fields.add(field.get(this));
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        return formatAst(this.getClass().getSimpleName(), fields);
    }

    private static String formatAst(String name, Object... arguments) {
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

    public Span span() {
        return span;
    }
}
