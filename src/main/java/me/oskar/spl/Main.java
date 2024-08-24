package me.oskar.spl;

import me.oskar.spl.analysis.name.NameAnalysis;
import me.oskar.spl.analysis.semantic.SemanticAnalysis;
import me.oskar.spl.codegen.Target;
import me.oskar.spl.error.Error;
import me.oskar.spl.lexer.Lexer;
import me.oskar.spl.parser.Parser;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.Callable;

@Command(name = "splc", mixinStandardHelpOptions = true, version = "splc 1.0")
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "Source code file to compile.")
    private File input;

    @Option(names = {"-o", "--output"}, description = "File to write output to.")
    private File output;

    @Option(names = {"-t", "--target"}, required = true, description = "The target to generate code for (${COMPLETION-CANDIDATES}).")
    private Target target;

    @Option(names = {"--headless"}, description = "Disable rendering procedures.")
    private boolean headless;

    @Override
    public Integer call() throws Exception {
        if (output == null) {
            output = new File(input.getName().split("\\.")[0] + "." + target.extension);
        }

        String code;
        try {
            code = new String(Files.readAllBytes(input.toPath()));
        } catch (NoSuchFileException e) {
            System.out.printf("File %s could not be read", input.getPath());
            return 1;
        }

        var error = new Error(code, input.getPath());

        var lexer = new Lexer(code);

        var parser = new Parser(lexer, error);
        parser.generateAst();
        var program = parser.getProgram();

        var nameAnalysis = new NameAnalysis(false);
        var globalTable = nameAnalysis.buildSymbolTable(program, error, target, headless);

        var semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.checkProcedures(program, globalTable, error, target);

        target.variableAllocation.allocateVariables(program, globalTable, target);

        var writer = new PrintWriter(new FileWriter(output));
        target.codeGenerator.generateCode(program, globalTable, writer, headless);
        writer.flush();

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }
}