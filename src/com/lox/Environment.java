package com.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores variable bindings and supports nested scopes.
 *
 * <p>Each block in Lox creates a new environment whose {@link #enclosing}
 * reference points to the outer scope. When a variable is read or assigned,
 * the lookup walks up the enclosing chain until the name is found.</p>
 */
class Environment {
    /**
     * The outer environment, or {@code null} for the global scope.
     */
    final Environment enclosing;

    /**
     * Map from variable name to its current value.
     */
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Creates the global environment with no enclosing scope.
     */
    Environment() {
        enclosing = null;
    }

    /**
     * Creates a new nested environment.
     *
     * @param enclosing the outer environment
     */
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Defines a new variable in this environment.
     *
     * <p>Defining a variable always happens in the current scope, even if the
     * same name exists in an outer scope.</p>
     *
     * @param name  the variable name
     * @param value the variable's initial value
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Looks up a variable by name, walking up the enclosing chain if needed.
     *
     * @param name the token whose lexeme names the variable
     * @return the variable's current value
     * @throws RuntimeError if the variable is not defined
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
    }

    /**
     * Assigns a new value to an existing variable, walking up the enclosing
     * chain if needed.
     *
     * @param name  the token whose lexeme names the variable
     * @param value the value to store
     * @throws RuntimeError if the variable is not defined
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Looks up a variable in an ancestor scope at a known static distance.
     *
     * <p>The resolver pre-computes how many scopes outward a variable lives;
     * this method uses that distance to avoid a dynamic name lookup.</p>
     *
     * @param distance the number of enclosing scopes to walk outward
     * @param name     the variable name
     * @return the variable's current value
     */
    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Assigns a value to a variable in an ancestor scope at a known static
     * distance.
     *
     * @param distance the number of enclosing scopes to walk outward
     * @param name     the token whose lexeme names the variable
     * @param value    the value to store
     */
    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    /**
     * Returns the ancestor environment {@code distance} scopes outward.
     *
     * @param distance the number of scopes to traverse
     * @return the environment at that depth
     */
    Environment ancestor(int distance) {
        Environment env = this;

        for (int i = 0; i < distance; i++) {
            env = env.enclosing;
        }

        return env;
    }
}
