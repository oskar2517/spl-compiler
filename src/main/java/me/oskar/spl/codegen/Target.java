package me.oskar.spl.codegen;

import me.oskar.spl.analysis.allocation.VariableAllocation;
import me.oskar.spl.analysis.allocation.js.JsVariableAllocation;
import me.oskar.spl.analysis.allocation.wasm.WasmVariableAllocation;
import me.oskar.spl.analysis.allocation.x86.X86VariableAllocation;
import me.oskar.spl.codegen.js.JsCodeGenerator;
import me.oskar.spl.codegen.js.NodeJsCodeGenerator;
import me.oskar.spl.codegen.wasm.WasmCodeGenerator;
import me.oskar.spl.codegen.x86.X86CodeGenerator;
import me.oskar.spl.type.PrimitiveType;
import me.oskar.spl.type.Type;

public enum Target {

    JS("js", new JsCodeGenerator(), new JsVariableAllocation(), 0),
    NODEJS("js", new NodeJsCodeGenerator(), new JsVariableAllocation(), 0),
    WASM("wat", new WasmCodeGenerator(), new WasmVariableAllocation(), 4),
    X86("nasm", new X86CodeGenerator(), new X86VariableAllocation(), 8);

    public final String extension;
    public final CodeGenerator codeGenerator;
    public final VariableAllocation variableAllocation;
    public final int wordSize;
    public final Type intType;
    public final Type boolType;

    Target(String extension, CodeGenerator codeGenerator, VariableAllocation variableAllocation, int wordSize) {
        this.extension = extension;
        this.codeGenerator = codeGenerator;
        this.variableAllocation = variableAllocation;
        this.wordSize = wordSize;

        this.intType = new PrimitiveType(wordSize, "int");
        this.boolType = new PrimitiveType(wordSize, "boolean");
    }
}