package com.lox;

/**
 * Exception thrown when the interpreter encounters a runtime error.
 *
 * <p>Carrying the offending token lets {@link Lox#runtimeError(RuntimeError)}
 * report the source line where the problem occurred.</p>
 */
class RuntimeError extends RuntimeException {
    /** The token being evaluated when the error occurred. */
    final Token token;

    /**
     * Creates a new runtime error.
     *
     * @param token   the token related to the error
     * @param message human-readable error description
     */
    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
