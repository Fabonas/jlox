package com.lox;

/**
 * Internal exception used to unwind the interpreter stack for return values.
 *
 * <p>When a Lox {@code return} statement is executed, the interpreter throws
 * a {@code Return} exception. {@link LoxFunction#call(Interpreter, List)}
 * catches it and extracts {@link #value} as the result of the call. This
 * avoids threading an explicit return value through every statement visitor
 * method.</p>
 */
class Return extends RuntimeException {

    /**
     * The value being returned, or {@code null} for a bare {@code return}.
     */
    final Object value;

    /**
     * Creates a new return exception.
     *
     * <p>The superclass is initialized without a message, cause, suppression,
     * or writable stack trace because this exception is used for control flow,
     * not error reporting.</p>
     *
     * @param value the value to return
     */
    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
