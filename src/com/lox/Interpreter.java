package com.lox;


import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Tree-walking interpreter for Lox.
 *
 * <p>Implements both {@link Expr.Visitor} and {@link Stmt.Visitor} to evaluate
 * expressions and execute statements. The interpreter uses Java's own call stack
 * for recursive evaluation and an {@link Environment} chain for variable storage.</p>
 *
 * <p>Runtime errors (such as dividing by zero or using an undefined variable)
 * are reported through {@link Lox#runtimeError(RuntimeError)}.</p>
 */
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    /**
     * The global (top-level) environment, created once and shared by every
     * program run through this interpreter.
     */
    final Environment global = new Environment();

    /**
     * The current environment for variable lookups.
     */
    /**
     * The current environment for variable lookups. Starts as {@link #global}
     * and changes when entering blocks or function calls.
     */
    private Environment env = global;
    /**
     * Maps each local variable expression to its lexical scope depth. The
     * resolver fills this map; the interpreter uses it to fetch values from
     * the correct ancestor environment.
     */
    private final Map<Expr, Integer> locals = new HashMap<>();

    /**
     * Creates the interpreter and defines native functions in the global
     * environment. Currently registers {@code clock}, which returns the current
     * wall-clock time in seconds.
     */
    Interpreter() {
        global.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter inter, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    /**
     * Interprets a list of statements.
     *
     * @param stmts the top-level statements to execute
     */
    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Records the static scope depth of a local variable reference.
     *
     * @param expr  the expression that refers to the variable
     * @param depth the number of enclosing scopes between the reference and
     *              the declaration
     */
    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    /**
     * Dispatches a statement to its visitor method.
     *
     * @param stmt the statement to execute
     */
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Converts a runtime value into a Lox-style string.
     *
     * <p>{@code nil} becomes {@code "nil"}, doubles drop a trailing {@code .0},
     * and everything else uses its Java {@code toString()}.</p>
     *
     * @param value the runtime value
     * @return the string representation
     */
    private String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return value.toString();
    }

    /**
     * Executes an expression statement by evaluating and discarding its value.
     */
    @Override
    public Object visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    /**
     * Executes a print statement by evaluating and printing its value.
     */
    @Override
    public Object visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /**
     * Returns the literal value stored in a literal expression.
     */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    /**
     * Evaluates a grouping by evaluating its inner expression.
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    /**
     * Dispatches an expression to its visitor method.
     *
     * @param expr the expression to evaluate
     * @return the expression's runtime value
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Evaluates a unary expression.
     *
     * <p>Supports logical negation ({@code !}) and arithmetic negation ({@code -}).</p>
     */
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        return null;
    }

    /**
     * Throws a {@link RuntimeError} if the operand is not a number.
     *
     * @param operator the operator token, used for error reporting
     * @param operand  the value to check
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Determines the truthiness of a Lox value.
     *
     * <p>{@code nil} and {@code false} are falsy; everything else is truthy.</p>
     *
     * @param obj the value to test
     * @return whether the value is truthy
     */
    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean) obj;

        return true;
    }

    /**
     * Evaluates a binary expression.
     *
     * <p>Supports arithmetic, comparison, equality, and string concatenation.
     * Division by zero is treated as a runtime error.</p>
     */
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESSER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;
            case LESSER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if (left instanceof String && right instanceof Double) {
                    return (String) left + ((Double) right).toString();
                }

                if (left instanceof Double && right instanceof String) {
                    return ((Double) left).toString() + (String) right;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers, two strings, or a string and a number.");
        }

        return null;
    }

    /**
     * Validates that both operands are numbers and checks for division by zero.
     *
     * @param operator the operator token, used for error reporting
     * @param left     the left operand
     * @param right    the right operand
     */
    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            if (operator.type == TokenType.SLASH && (double) right == 0.0) {
                throw new RuntimeError(operator, "Division by zero.");
            }

            return;
        }

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Compares two Lox values for equality.
     *
     * @param left  the left value
     * @param right the right value
     * @return {@code true} if the values are equal
     */
    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;

        return left.equals(right);
    }

    /**
     * Executes a variable declaration by defining it in the environment.
     */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.init != null) {
            value = evaluate(stmt.init);
        }

        env.define(stmt.name.lexeme, value);
        return null;
    }

    /**
     * Evaluates an assignment and stores the result in the environment.
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer dist = locals.get(expr);

        if (dist != null) {
            env.assignAt(dist, expr.name, value);
        } else {
            global.assign(expr.name, value);
        }

        return value;
    }

    /**
     * Looks up a variable by name in the environment chain.
     */
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    /**
     * Looks up a variable using its pre-computed lexical depth.
     *
     * @param name the variable's identifier token
     * @param expr the expression that refers to the variable
     * @return the variable's value
     */
    private Object lookUpVariable(Token name, Expr expr) {
        Integer dist = locals.get(expr);

        if (dist != null) {
            return env.getAt(dist, name.lexeme);
        } else {
            return global.get(name);
        }
    }

    /**
     * Executes a block by creating a new environment nested in the current one.
     */
    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(env));
        return null;
    }

    @Override
    public Object visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;

        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);

            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }

        env.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            env = new Environment(env);
            env.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();

        for (Stmt.Function method : stmt.methods) {
            LoxFunction func = new LoxFunction(method, env, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, func);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass) superclass, methods);

        if (superclass != null) {
            env = env.enclosing;
        }

        env.assign(stmt.name, klass);

        return null;
    }

    /**
     * Executes a list of statements inside a given environment.
     *
     * <p>The previous environment is restored in a {@code finally} block so that
     * even a future {@code return} or exception cannot leak scope.</p>
     *
     * @param stmts the statements to execute
     * @param env   the environment to use for the block
     */
    void executeBlock(List<Stmt> stmts, Environment env) {
        Environment prev = this.env;

        try {
            this.env = env;

            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.env = prev;
        }
    }

    /**
     * Executes the then or else branch of an if statement based on condition truthiness.
     */
    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }

        return null;
    }

    /**
     * Evaluates a logical {@code and} or {@code or} expression with short-circuiting.
     */
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object obj = evaluate(expr.object);

        if (!(obj instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance) obj).set(expr.name, value);

        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int dist = locals.get(expr);

        LoxClass superclass = (LoxClass) env.getAt(dist, "super");

        LoxInstance obj = (LoxInstance) env.getAt(dist - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        return method.bind(obj);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    /**
     * Repeatedly executes the body while the condition is truthy.
     */
    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.body);
        }

        return null;
    }

    /**
     * Evaluates a function call expression.
     *
     * <p>The callee is evaluated, the arguments are evaluated in order, and
     * then the callable is invoked. Arity mismatches are reported as runtime
     * errors.</p>
     *
     * @param expr the call expression
     * @return the value returned by the callable
     */
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof LoxCallable func)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        if (args.size() != func.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + func.arity() + " arguments, but got " + args.size() + ".");
        }

        return func.call(this, args);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object obj = evaluate(expr.object);

        if (obj instanceof LoxInstance) {
            return ((LoxInstance) obj).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    /**
     * Executes a return statement by evaluating its value and throwing a
     * {@link Return} control-flow exception.
     *
     * @param stmt the return statement
     */
    @Override
    public Object visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    /**
     * Executes a function declaration by creating a {@link LoxFunction}
     * closure and binding it to the function's name in the current
     * environment.
     *
     * @param stmt the function declaration
     */
    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction func = new LoxFunction(stmt, env, false);
        env.define(stmt.name.lexeme, func);

        return null;
    }
}
