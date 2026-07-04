package com.lox;

import java.util.List;

/**
 * Interface implemented by everything that can be called as a function in Lox.
 *
 * <p>Both native functions (such as {@code clock}) and user-defined
 * {@link LoxFunction} objects implement this interface. The interpreter checks
 * the arity before invoking {@link #call(Interpreter, List)}.</p>
 */
interface LoxCallable {

    /**
     * Returns the number of arguments this callable expects.
     *
     * @return the expected argument count
     */
    int arity();

    /**
     * Invokes this callable with the given interpreter and arguments.
     *
     * @param inter the interpreter executing the call
     * @param args  the evaluated argument values
     * @return the value returned by the callable, or {@code null}
     */
    Object call(Interpreter inter, List<Object> args);
}
