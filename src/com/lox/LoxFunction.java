package com.lox;

import java.util.List;

/**
 * Runtime representation of a user-defined Lox function.
 *
 * <p>Each function captures the environment that was active when it was
 * declared, creating a closure. When called, it binds the supplied arguments
 * to parameter names in a new environment nested inside that closure and then
 * executes the function body.</p>
 */
class LoxFunction implements LoxCallable {

    /**
     * The parsed function declaration.
     */
    private final Stmt.Function decl;

    /**
     * The environment captured at declaration time; the closure for this
     * function.
     */
    private final Environment closure;
    private final boolean isInit;

    /**
     * Creates a new function object bound to its declaring closure.
     *
     * @param decl    the parsed function declaration
     * @param closure the environment in which the function was declared
     */
    LoxFunction(Stmt.Function decl, Environment closure, boolean isInit) {
        this.closure = closure;
        this.decl = decl;
        this.isInit = isInit;
    }

    /**
     * Executes the function body in a new environment.
     *
     * <p>A {@link Return} exception thrown by the body carries the value back
     * to the caller. If the body completes normally, the call returns
     * {@code null}.</p>
     *
     * @param inter the interpreter executing the call
     * @param args  the evaluated argument values
     * @return the function's return value, or {@code null}
     */
    @Override
    public Object call(Interpreter inter, List<Object> args) {
        Environment env = new Environment(closure);

        for (int i = 0; i < decl.params.size(); i++) {
            env.define(decl.params.get(i).lexeme, args.get(i));
        }

        try {
            inter.executeBlock(decl.body, env);
        } catch (Return returnValue) {
            if (isInit) return closure.getAt(0, "this");
            return returnValue.value;
        }
        ;
        if (isInit) return closure.getAt(0, "this");
        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(decl, env, isInit);
    }

    /**
     * Returns the number of parameters this function accepts.
     *
     * @return the parameter count
     */
    @Override
    public int arity() {
        return decl.params.size();
    }

    /**
     * Returns a short display string for the function.
     *
     * @return a string of the form {@code <fn name>}
     */
    @Override
    public String toString() {
        return "<fn " + decl.name.lexeme + ">";
    }
}
