package me.oskar.spl;

import me.oskar.spl.analysis.allocation.VariableAllocation;
import me.oskar.spl.analysis.allocation.js.JsVariableAllocation;
import me.oskar.spl.analysis.allocation.wasm.WasmVariableAllocation;
import me.oskar.spl.analysis.allocation.x86.X86VariableAllocation;
import me.oskar.spl.analysis.name.initializer.TableInitializer;
import me.oskar.spl.analysis.name.initializer.UnpositionedTableInitializer;
import me.oskar.spl.analysis.name.initializer.X86TableInitializer;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.js.JsCodeGenerator;
import me.oskar.spl.codegen.js.NodeJsCodeGenerator;
import me.oskar.spl.codegen.wasm.WasmCodeGenerator;
import me.oskar.spl.codegen.x86.X86CodeGenerator;
import me.oskar.spl.type.PrimitiveType;
import me.oskar.spl.type.Type;

public enum Target {

    JS("js", new JsCodeGenerator(), new JsVariableAllocation(), new UnpositionedTableInitializer(), 0),
    NODEJS("js", new NodeJsCodeGenerator(), new JsVariableAllocation(), new UnpositionedTableInitializer(), 0),
    WASM("wat", new WasmCodeGenerator(), new WasmVariableAllocation(), new UnpositionedTableInitializer(), 4),
    X86("nasm", new X86CodeGenerator(), new X86VariableAllocation(), new X86TableInitializer(), 8);

    public final String extension;
    public final CodeGenerator codeGenerator;
    public final VariableAllocation variableAllocation;
    public final TableInitializer tableInitializer;
    public final int wordSize;
    public final Type intType;
    public final Type boolType;

    Target(String extension, CodeGenerator codeGenerator, VariableAllocation variableAllocation,
           TableInitializer tableInitializer, int wordSize) {
        this.extension = extension;
        this.codeGenerator = codeGenerator;
        this.variableAllocation = variableAllocation;
        this.tableInitializer = tableInitializer;
        this.wordSize = wordSize;

        this.intType = new PrimitiveType(wordSize, "int");
        this.boolType = new PrimitiveType(wordSize, "boolean");
    }
}