package com.lox;

import java.util.*;

/**
 * Static scope resolver for Lox.
 *
 * <p>The resolver walks the AST after parsing but before interpretation. It
 * tracks which scope every variable belongs to and tells the interpreter the
 * exact lexical distance to each variable. This turns variable access from a
 * slow dynamic chain walk into a fast indexed lookup at runtime.</p>
 *
 * <p>The resolver also performs a few static checks:</p>
 * <ul>
 *   <li>Detects a local variable being read inside its own initializer.</li>
 *   <li>Reports a {@code return} statement used outside any function.</li>
 *   <li>Flags duplicate variable declarations in the same local scope.</li>
 * </ul>
 */
class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    /**
     * The interpreter that will receive the resolved variable distances.
     */
    private final Interpreter inter;

    /**
     * Stack of scopes currently in scope. Each scope maps a variable name to
     * {@code true} (defined and available) or {@code false} (declared but not
     * yet defined).
     */
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    /**
     * Tracks whether the resolver is currently inside a function body.
     */
    private enum FunctionType {

        NONE,
        FUNC,
        METHOD,
        INIT
    }

    private FunctionType currFunction = FunctionType.NONE;

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    private ClassType currentClass = ClassType.NONE;

    /**
     * Creates a new resolver for the given interpreter.
     *
     * @param inter the interpreter that will execute the resolved program
     */
    Resolver(Interpreter inter) {
        this.inter = inter;
    }

    /**
     * Resolves a list of top-level statements.
     *
     * @param stmts the statements to resolve
     */
    void resolve(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            resolve(stmt);
        }
    }

    /**
     * Resolves a single statement by dispatching to its visitor method.
     *
     * @param stmt the statement to resolve
     */
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Resolves a single expression by dispatching to its visitor method.
     *
     * @param expr the expression to resolve
     */
    private void resolve(Expr expr) {
        expr.accept(this);
    }

    /**
     * Enters a new local scope.
     */
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    /**
     * Exits the current local scope.
     */
    private void endScope() {
        scopes.pop();
    }

    /**
     * Declares a variable in the current scope without yet defining it.
     *
     * <p>Between declaration and definition, the variable is marked
     * {@code false} so that reading it in its own initializer can be detected.</p>
     *
     * @param name the variable's identifier token
     */
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    /**
     * Marks a previously declared variable as fully defined and usable.
     *
     * @param name the variable's identifier token
     */
    private void define(Token name) {
        if (scopes.isEmpty()) return;

        scopes.peek().put(name.lexeme, true);
    }

    /**
     * Resolves a local variable reference.
     *
     * <p>Searches outward from the innermost scope. If found, the distance to
     * that scope is recorded in the interpreter.</p>
     *
     * @param expr the expression that refers to the variable
     * @param name the variable's identifier token
     */
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                inter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }

        // Not found in any local scope: assume it is global.
    }

    /**
     * Resolves a function declaration and its body in a new scope.
     *
     * @param func the function declaration
     * @param type the kind of function context being entered
     */
    private void resolveFunction(Stmt.Function func, FunctionType type) {
        FunctionType enclosingFunction = currFunction;
        currFunction = type;

        beginScope();

        for (Token param : func.params) {
            declare(param);
            define(param);
        }

        resolve(func.body);

        endScope();
        currFunction = enclosingFunction;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();

        resolve(stmt.statements);

        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);

        if (stmt.init != null) {
            resolve(stmt.init);
        }

        define(stmt.name);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name, "Cannot read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNC);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.cond);
        resolve(stmt.thenBranch);

        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            if (currFunction == FunctionType.INIT) {
                Lox.error(stmt.keyword, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.cond);
        resolve(stmt.body);

        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr arg : expr.arguments) {
            resolve(arg);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);

        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.");

            return null;
        }

        resolveLocal(expr, expr.keyword);

        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            Lox.error(stmt.superclass.name, "A class can't inherit from itself.");
        }

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType decl = FunctionType.METHOD;

            if (method.name.lexeme.equals("init")) {
                decl = FunctionType.INIT;
            }

            resolveFunction(method, decl);
        }

        currentClass = enclosingClass;
        endScope();

        if (stmt.superclass != null) endScope();

        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'super' outside of a class.");
        } else if (currentClass == ClassType.CLASS) {
            Lox.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }
}
