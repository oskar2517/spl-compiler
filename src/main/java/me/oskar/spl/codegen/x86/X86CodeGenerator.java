package me.oskar.spl.codegen.x86;

import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;

import java.io.PrintWriter;

public class X86CodeGenerator implements CodeGenerator {

    @Override
    public void generateCode(Program program, SymbolTable symbolTable, PrintWriter printWriter, boolean headless) {
        var output = new CodePrinter(printWriter, 4);

        output.println("section .text");
        output.incIndentLevel();
        output.println("global main");
        output.decIndentLevel();
        output.println();
        output.incIndentLevel();
        output.println("extern printf");
        output.println("extern exit");
        output.println("extern __init_time");
        if (!headless) {
            output.println("extern __sdl_init_screen");
            output.println("extern __sdl_event_loop");
        }

        for (var entryPair : symbolTable.entrySet()) {
            var entryName = entryPair.getKey();
            var symbolTableEntry = entryPair.getValue();

            if (symbolTableEntry instanceof ProcedureEntry pe && pe.isPredefinedProcedure()) {
                output.println("extern _%s", entryName);
            }
        }

        output.decIndentLevel();
        output.println();

        output.println("__arrayAccessOutOfBounds:");
        output.incIndentLevel();
        output.println("xor rax, rax");
        output.println("mov rdi, array_oob_error");
        output.println("call printf");
        output.println("mov rdi, 1");
        output.println("call exit");
        output.decIndentLevel();
        output.println();

        output.println("main:");
        output.incIndentLevel();
        output.println("push rbp");
        output.println("mov rbp, rsp");
        output.decIndentLevel();
        output.println();
        output.incIndentLevel();
        output.println("call __init_time");
        if (!headless) {
            output.println("call __sdl_init_screen");
        }
        output.println("call _main");
        if (!headless) {
            output.println("call __sdl_event_loop");
        }
        output.decIndentLevel();
        output.println();
        output.incIndentLevel();
        output.println("mov rdi, 0");
        output.println("call exit");
        output.println("pop rbp");
        output.println("ret");
        output.decIndentLevel();
        output.println();

        var codeGenerator = new X86CodeGeneratorVisitor(symbolTable, output);
        program.accept(codeGenerator);

        output.println("section .data");
        output.incIndentLevel();
        output.println("array_oob_error: db \"error: array access out of bounds\",10,0");
        output.decIndentLevel();
    }
}
