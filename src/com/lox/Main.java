package com.lox;

import java.io.IOException;

/**
 * Alternative entry point for the jlox interpreter.
 *
 * <p>This class simply delegates to {@link Lox#main(String[])} so the interpreter
 * can be launched either as {@code com.lox.Lox} or {@code com.lox.Main}.</p>
 */
public class Main {

    /**
     * Delegates to {@link Lox#main(String[])}.
     *
     * @param args command-line arguments
     * @throws IOException if reading input fails
     */
    public static void main(String[] args) throws IOException {
        Lox.main(args);
    }
}
