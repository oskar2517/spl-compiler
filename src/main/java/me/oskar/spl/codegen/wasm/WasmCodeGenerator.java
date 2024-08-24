package me.oskar.spl.codegen.wasm;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;

import java.io.PrintWriter;

public class WasmCodeGenerator implements CodeGenerator {

    @Override
    public void generateCode(Program program, SymbolTable table, PrintWriter printWriter, boolean headless) {
        var output = new CodePrinter(printWriter, 4);

        output.println("(module");
        output.incIndentLevel();

        for (var entryPair : table.entrySet()) {
            var entryName = entryPair.getKey();
            var symbolTableEntry = entryPair.getValue();

            if (symbolTableEntry instanceof ProcedureEntry pe && pe.isPredefinedProcedure()) {
                var importString = String.format("(import \"env\" \"%s\" (func $_%s", entryName, entryName) +
                        " (param i32)".repeat(pe.getParameterTypes().size()) +
                        "))";

                output.println(importString);
            }
        }

        output.println("(import \"env\" \"memory\" (memory 1 100 shared))");
        output.println();

        output.println("(func $checkArrayIndex (param $index i32) (param $arraySize i32) (result i32)");
        output.println("    (if (i32.lt_s (local.get $index) (i32.const 0))");
        output.println("        (then");
        output.println("            unreachable");
        output.println("        )");
        output.println("    )");
        output.println("    (if (i32.ge_s (local.get $index) (local.get $arraySize))");
        output.println("        (then");
        output.println("            unreachable");
        output.println("        )");
        output.println("    )");
        output.println("    local.get $index");
        output.println("    return");
        output.println(")");
        output.println();

        output.println("(func $main");
        output.println("    (i32.store (i32.const 0) (i32.const 4))");
        output.println("    call $_main");
        output.println(")");
        output.println();

        var codeGeneratorVisitor = new WasmCodeGeneratorVisitor(table, output);
        program.accept(codeGeneratorVisitor);

        output.println("(export \"main\" (func $main))");
        output.decIndentLevel();
        output.println(")");
    }

}
