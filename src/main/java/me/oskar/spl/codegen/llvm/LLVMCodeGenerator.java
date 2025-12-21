package me.oskar.spl.codegen.llvm;

import me.oskar.spl.Target;
import me.oskar.spl.ast.Program;
import me.oskar.spl.codegen.CodeGenerator;
import me.oskar.spl.codegen.CodePrinter;
import me.oskar.spl.table.ParameterType;
import me.oskar.spl.table.ProcedureEntry;
import me.oskar.spl.table.SymbolTable;
import me.oskar.spl.type.ArrayType;
import me.oskar.spl.type.Type;

import java.io.PrintWriter;
import java.util.stream.Collectors;

public class LLVMCodeGenerator implements CodeGenerator {

    public static String convertTypeToLLVM(Target target, Type type) {
        if (type == target.intType) {
            return "i64";
        } else if (type == target.boolType) {
            return "i1";
        } else if (type instanceof ArrayType at) {
            return "[%s x %s]".formatted(at.getArraySize(), convertTypeToLLVM(target, at.getBaseType()));
        }

        throw new IllegalStateException("Unknown type %s".formatted(type));
    }

    public static String mapParameterTypeToLLVMType(Target target, ParameterType p) {
        if (p.isReference()) {
            return convertTypeToLLVM(target, p.getType()) + "*";
        }

        return convertTypeToLLVM(target, p.getType());
    }

    private String generateSignature(Target target, String procName, ProcedureEntry entry) {
        var parameterTypes = entry.getParameterTypes().stream()
                .map(p -> mapParameterTypeToLLVMType(target, p))
                .collect(Collectors.joining(", "));

        return "void @_%s(%s)".formatted(procName, parameterTypes);
    }

    @Override
    public void generateCode(Program program, SymbolTable symbolTable, PrintWriter printWriter, Target target,
                             boolean headless) {
        var output = new CodePrinter(printWriter, 4);

        for (var entryPair : symbolTable.entrySet()) {
            var entryName = entryPair.getKey();
            var symbolTableEntry = entryPair.getValue();

            if (symbolTableEntry instanceof ProcedureEntry pe && pe.isPredefinedProcedure()) {
                output.println("declare %s", generateSignature(target, entryName, pe));
            }
        }

        output.println();

        var codeGenerator = new LLVMCodeGeneratorVisitor(symbolTable, output, target);
        program.accept(codeGenerator);

        output.println("define i32 @main() {");
        output.println("entry:");
        output.incIndentLevel();
        output.println("call void @_main()");
        output.println("ret i32 0");
        output.decIndentLevel();
        output.println("}");
    }
}
