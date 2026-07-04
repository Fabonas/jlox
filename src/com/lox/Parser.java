package com.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.function.Supplier;

import static com.lox.TokenType.*;

/**
 * Recursive-descent parser for Lox.
 *
 * <p>Consumes the flat token stream produced by the {@link Scanner} and builds
 * a tree of {@link Expr} and {@link Stmt} nodes. Each grammar rule is encoded
 * as one method, following the precedence levels described in
 * <em>Crafting Interpreters</em>.</p>
 *
 * <p>When a syntax error is encountered, the parser reports it through
 * {@link Lox#error(Token, String)} and attempts to resynchronize at the next
 * statement boundary so that later code can still be parsed.</p>
 */
class Parser {

    /**
     * Simple unchecked exception used internally to unwind to a sync point.
     */
    private static class ParseError extends RuntimeException {
    }

    /**
     * The tokens to parse.
     */
    private final List<Token> tokens;

    /**
     * Index of the token currently being parsed.
     */
    private int current = 0;

    /**
     * Tracks how many nested loop bodies the parser is currently inside.
     *
     * <p>This is reserved for future use (for example, enforcing that
     * {@code break} only appears inside a loop). It is not used by the current
     * language implementation.</p>
     */
    private int loopDepth = 0;

    /**
     * Creates a parser over the given token list.
     *
     * @param tokens the scanned tokens
     */
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses the token list into a list of top-level statements.
     *
     * @return the parsed statements; {@code null} entries may appear where
     * error recovery dropped a malformed declaration
     */
    List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();

        while (!isAtEnd()) {
            stmts.add(declaration());
        }

        return stmts;
    }


    /**
     * Parses a declaration or statement.
     *
     * <p>This is the top-level grammar rule. If parsing fails, the parser
     * synchronizes and returns {@code null} so that the remaining program can
     * still be parsed.</p>
     *
     * @return the parsed statement, or {@code null} on error
     */
    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(LESSER)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' before class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    /**
     * Parses a function declaration or anonymous function body.
     *
     * @param kind human-readable kind of function, such as "function"
     * @return the parsed {@link Stmt.Function}
     */
    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");
        consume(LEFT_PAREN, "Expected '(' after " + kind + " name.");
        List<Token> params = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                params.add(consume(IDENTIFIER, "Expected parameter name."));
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expected ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    /**
     * Parses a variable declaration: {@code var name = initializer;}.
     *
     * @return the parsed {@link Stmt.Var}
     */
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr init = null;
        if (match(EQUAL)) {
            init = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, init);
    }

    /**
     * Parses any non-declaration statement.
     *
     * @return the parsed statement
     */
    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatements();
    }

    /**
     * Parses a return statement.
     *
     * <p>The return value is optional; a bare {@code return} produces
     * {@code null} at runtime.</p>
     *
     * @return the parsed {@link Stmt.Return}
     */
    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    /**
     * Parses a {@code for} loop.
     *
     * <p>Desugars the classic C-style {@code for} into a {@link Stmt.Block}
     * containing the initializer, a {@link Stmt.While} for the condition and
     * body, and an increment expression appended to the body.</p>
     *
     * @return the parsed/desugared statement
     */
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt init;
        if (match(SEMICOLON)) {
            init = null;
        } else if (match(VAR)) {
            init = varDeclaration();
        } else {
            init = expressionStatements();
        }

        Expr cond = null;
        if (!check(SEMICOLON)) {
            cond = expression();
        }

        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr inc = null;
        if (!check(RIGHT_PAREN)) {
            inc = expression();
        }

        consume(RIGHT_PAREN, "Expected ')' after for clauses.");

        Stmt body = statement();

        if (inc != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(inc)));
        }

        if (cond == null) {
            cond = new Expr.Literal(true);
        }

        body = new Stmt.While(cond, body);

        if (init != null) {
            body = new Stmt.Block(Arrays.asList(init, body));
        }

        return body;
    }

    /**
     * Parses an {@code if} statement with an optional {@code else} branch.
     *
     * @return the parsed {@link Stmt.If}
     */
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(cond, thenBranch, elseBranch);
    }

    /**
     * Parses a {@code print} statement.
     *
     * @return the parsed {@link Stmt.Print}
     */
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(value);
    }

    /**
     * Parses a {@code while} loop.
     *
     * @return the parsed {@link Stmt.While}
     */
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition.");
        Stmt body = statement();

        return new Stmt.While(cond, body);
    }

    /**
     * Parses an expression followed by a semicolon.
     *
     * @return the parsed {@link Stmt.Expression}
     */
    private Stmt expressionStatements() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * Parses an expression, starting at the lowest-precedence rule.
     *
     * @return the parsed expression
     */
    private Expr expression() {
        return assignment();
    }

    /**
     * Parses an assignment expression.
     *
     * <p>The left-hand side is parsed as a higher-precedence expression first;
     * if an {@code =} follows and the left side is a variable, it is rewritten
     * into an {@link Expr.Assign}.</p>
     *
     * @return the parsed expression
     */
    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    /**
     * Parses a sequence of {@code or} expressions.
     *
     * @return the parsed expression
     */
    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses a sequence of {@code and} expressions.
     *
     * @return the parsed expression
     */
    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses equality expressions: {@code ==} and {@code !=}.
     *
     * @return the parsed expression
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses comparison expressions: {@code >}, {@code >=}, {@code <}, {@code <=}.
     *
     * @return the parsed expression
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESSER, LESSER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses addition and subtraction.
     *
     * @return the parsed expression
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses multiplication and division.
     *
     * @return the parsed expression
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Parses unary expressions: {@code !} and {@code -}.
     *
     * @return the parsed expression
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    /**
     * Parses a call expression, handling repeated calls for higher-order
     * expressions such as {@code f()()}.
     *
     * @return the parsed expression
     */
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expected property name after '.'.");

                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Parses the argument list of a function call.
     *
     * @param callee the expression being called
     * @return the parsed {@link Expr.Call}
     */
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) error(peek(), "Can't have more than 255 args.");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    /**
     * Parses primary expressions: literals, identifiers, grouping, and keywords.
     *
     * @return the parsed expression
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(THIS)) return new Expr.This(previous());
        if (match(IDENTIFIER)) return new Expr.Variable(previous());
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expected '.' after 'super'.");
            Token method = consume(IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression: ");
    }

    /**
     * Parses a braced block of statements.
     *
     * @return the list of statements inside the block
     */
    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after statements");
        return stmts;
    }

    /**
     * Consumes the current token if its type is one of {@code types}.
     *
     * @param types candidate token types
     * @return {@code true} if a token was consumed
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Consumes the current token if it has the expected type; otherwise reports
     * an error and throws {@link ParseError}.
     *
     * @param type    the expected token type
     * @param message error message if the token does not match
     * @return the consumed token
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Reports a parse error and returns a {@link ParseError} to throw.
     *
     * @param token   the offending token
     * @param message the error message
     * @return a fresh {@link ParseError}
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Discards tokens until the parser is positioned at the start of a new
     * statement, giving up on the current malformed statement.
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Consumes and returns the current token.
     *
     * @return the consumed token
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Returns {@code true} if the current token is {@link TokenType#EOF}.
     *
     * @return whether parsing has reached the end of input
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return the current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the most recently consumed token.
     *
     * @return the previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Returns {@code true} if the current token has the given type.
     *
     * @param type the type to test
     * @return whether the current token matches
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
}
