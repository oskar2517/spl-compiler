package me.oskar.spl.codegen.js;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;

import java.io.PrintWriter;

public class JsCodeGenerator implements CodeGenerator {

    @Override
    public void generateCode(Program program, SymbolTable table, PrintWriter printWriter, boolean headless) {
        var output = new CodePrinter(printWriter, 4);

        for (var entryPair : table.entrySet()) {
            var entryName = entryPair.getKey();
            var symbolTableEntry = entryPair.getValue();

            if (symbolTableEntry instanceof ProcedureEntry pe && pe.isPredefinedProcedure()) {
                output.println(String.format("const _%s = jsContext.%s;", entryName, entryName));
            }
        }

        output.println();
        output.println("function checkArrayIndex(index, arraySize) {");
        output.println("    if (index < 0 || index >= arraySize) {");
        output.println("        throw new Error(\"array access out of bounds\");");
        output.println("    }");
        output.println("    return index;");
        output.println("}");
        output.println();

        var codeGeneratorVisitor = new JsCodeGeneratorVisitor(table, output);
        program.accept(codeGeneratorVisitor);
    }
}
