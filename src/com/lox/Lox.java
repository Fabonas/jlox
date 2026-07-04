package com.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point and error reporter for the jlox interpreter.
 *
 * <p>Lox can run in two modes:</p>
 * <ul>
 *   <li><strong>Script mode:</strong> {@code jlox script.lox} reads and executes a file.</li>
 *   <li><strong>REPL mode:</strong> {@code jlox} with no arguments starts an interactive prompt.</li>
 * </ul>
 *
 * <p>It also centralizes compile-time and runtime error reporting so that the
 * scanner, parser, and interpreter can report problems using a single, consistent
 * format.</p>
 */
public class Lox {
    /**
     * Shared interpreter instance used for the whole session.
     */
    private static final Interpreter interpreter = new Interpreter();

    /**
     * Set to {@code true} when any compile-time error is reported.
     */
    static boolean hadError = false;

    /**
     * Set to {@code true} when a runtime error occurs.
     */
    static boolean hadRuntimeError = false;

    /**
     * Runs jlox either as a REPL or as a file interpreter.
     *
     * @param args command-line arguments; at most one script path is allowed
     * @throws IOException if reading input fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Reads an entire source file and runs it.
     *
     * <p>Exits with status {@code 65} for compile errors and {@code 70} for
     * runtime errors, matching the standard Unix convention used by the book.</p>
     *
     * @param path path to the Lox source file
     * @throws IOException if the file cannot be read
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /**
     * Runs the read-eval-print loop interactively.
     *
     * <p>Each line is parsed and executed independently. Errors are reset after
     * every line so that a typo does not kill the session.</p>
     *
     * @throws IOException if reading from standard input fails
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    /**
     * Scans, parses, and interprets a single chunk of source code.
     *
     * @param source the Lox source code to run
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        if (hadError) return;

        interpreter.interpret(stmts);
    }

    /**
     * Reports a compile-time error at the given line.
     *
     * @param line    the source line where the error occurred
     * @param message human-readable error description
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Prints an error message and records that an error occurred.
     *
     * @param line    the source line of the error
     * @param where   additional location context, such as " at end" or " at 'x'"
     * @param message the error message
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    /**
     * Reports a compile-time error at the location of the given token.
     *
     * @param token   the token where the error occurred
     * @param message human-readable error description
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    /**
     * Reports a runtime error and records that it occurred.
     *
     * @param error the runtime error to report
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
