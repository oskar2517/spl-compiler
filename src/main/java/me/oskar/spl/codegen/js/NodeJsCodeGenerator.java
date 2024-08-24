package me.oskar.spl.codegen.js;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.prelude.Prelude;
import me.oskar.spl.table.SymbolTable;

import java.io.PrintWriter;

public class NodeJsCodeGenerator implements CodeGenerator {

    @Override
    public void generateCode(Program program, SymbolTable symbolTable, PrintWriter printWriter, boolean headless) {
        var output = new CodePrinter(printWriter, 4);

        output.println(Prelude.readNodeJsPrelude(headless));

        var codeGeneratorVisitor = new JsCodeGeneratorVisitor(symbolTable, output);
        program.accept(codeGeneratorVisitor);

        output.println("_main();");
    }
}
