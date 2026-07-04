package com.lox;

import java.util.List;

/**
 * Auto-generated abstract syntax tree base class for stmt nodes.
 *
 * <p>Do not edit by hand. Regenerate with {@code make ast}.</p>
 */
abstract class Stmt {
  /**
   * Visitor interface for Stmt nodes.
   *
   * @param <R> the type returned by every visit method
   */
  interface Visitor<R> {
    /**
     * Visits a Block stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitBlockStmt(Block stmt);
    /**
     * Visits a Class stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitClassStmt(Class stmt);
    /**
     * Visits a Expression stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitExpressionStmt(Expression stmt);
    /**
     * Visits a Function stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitFunctionStmt(Function stmt);
    /**
     * Visits a If stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitIfStmt(If stmt);
    /**
     * Visits a Print stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitPrintStmt(Print stmt);
    /**
     * Visits a While stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitWhileStmt(While stmt);
    /**
     * Visits a Return stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitReturnStmt(Return stmt);
    /**
     * Visits a Var stmt node.
     *
     * @param stmt the node to visit
     * @return the visitor's result
     */
    R visitVarStmt(Var stmt);
  }
  /**
   * Represents a Block stmt node.
   */
  static class Block extends Stmt {
    /**
     * Creates a new Block node.
     *
     * @param statements the statements child/operand
     */
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    /** The statements child/operand. */
    final List<Stmt> statements;
  }
  /**
   * Represents a Class stmt node.
   */
  static class Class extends Stmt {
    /**
     * Creates a new Class node.
     *
     * @param name the name child/operand
     * @param superclass the superclass child/operand
     * @param methods the methods child/operand
     */
    Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    /** The name child/operand. */
    final Token name;
    /** The superclass child/operand. */
    final Expr.Variable superclass;
    /** The methods child/operand. */
    final List<Stmt.Function> methods;
  }
  /**
   * Represents a Expression stmt node.
   */
  static class Expression extends Stmt {
    /**
     * Creates a new Expression node.
     *
     * @param expression the expression child/operand
     */
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    /** The expression child/operand. */
    final Expr expression;
  }
  /**
   * Represents a Function stmt node.
   */
  static class Function extends Stmt {
    /**
     * Creates a new Function node.
     *
     * @param name the name child/operand
     * @param params the params child/operand
     * @param body the body child/operand
     */
    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    /** The name child/operand. */
    final Token name;
    /** The params child/operand. */
    final List<Token> params;
    /** The body child/operand. */
    final List<Stmt> body;
  }
  /**
   * Represents a If stmt node.
   */
  static class If extends Stmt {
    /**
     * Creates a new If node.
     *
     * @param cond the cond child/operand
     * @param thenBranch the thenBranch child/operand
     * @param elseBranch the elseBranch child/operand
     */
    If(Expr cond, Stmt thenBranch, Stmt elseBranch) {
      this.cond = cond;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    /** The cond child/operand. */
    final Expr cond;
    /** The thenBranch child/operand. */
    final Stmt thenBranch;
    /** The elseBranch child/operand. */
    final Stmt elseBranch;
  }
  /**
   * Represents a Print stmt node.
   */
  static class Print extends Stmt {
    /**
     * Creates a new Print node.
     *
     * @param expression the expression child/operand
     */
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    /** The expression child/operand. */
    final Expr expression;
  }
  /**
   * Represents a While stmt node.
   */
  static class While extends Stmt {
    /**
     * Creates a new While node.
     *
     * @param cond the cond child/operand
     * @param body the body child/operand
     */
    While(Expr cond, Stmt body) {
      this.cond = cond;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    /** The cond child/operand. */
    final Expr cond;
    /** The body child/operand. */
    final Stmt body;
  }
  /**
   * Represents a Return stmt node.
   */
  static class Return extends Stmt {
    /**
     * Creates a new Return node.
     *
     * @param keyword the keyword child/operand
     * @param value the value child/operand
     */
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    /** The keyword child/operand. */
    final Token keyword;
    /** The value child/operand. */
    final Expr value;
  }
  /**
   * Represents a Var stmt node.
   */
  static class Var extends Stmt {
    /**
     * Creates a new Var node.
     *
     * @param name the name child/operand
     * @param init the init child/operand
     */
    Var(Token name, Expr init) {
      this.name = name;
      this.init = init;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    /** The name child/operand. */
    final Token name;
    /** The init child/operand. */
    final Expr init;
  }

  /**
   * Dispatches this node to the appropriate method on {@code visitor}.
   *
   * @param visitor the visitor to invoke
   * @param <R>     the visitor's result type
   * @return the value produced by the visitor
   */
  abstract <R> R accept(Visitor<R> visitor);
}
