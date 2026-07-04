package com.lox;

import java.util.List;

/**
 * Auto-generated abstract syntax tree base class for expr nodes.
 *
 * <p>Do not edit by hand. Regenerate with {@code make ast}.</p>
 */
abstract class Expr {
  /**
   * Visitor interface for Expr nodes.
   *
   * @param <R> the type returned by every visit method
   */
  interface Visitor<R> {
    /**
     * Visits a Assign expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitAssignExpr(Assign expr);
    /**
     * Visits a Binary expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitBinaryExpr(Binary expr);
    /**
     * Visits a Call expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitCallExpr(Call expr);
    /**
     * Visits a Get expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitGetExpr(Get expr);
    /**
     * Visits a Grouping expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitGroupingExpr(Grouping expr);
    /**
     * Visits a Literal expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitLiteralExpr(Literal expr);
    /**
     * Visits a Logical expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitLogicalExpr(Logical expr);
    /**
     * Visits a Set expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitSetExpr(Set expr);
    /**
     * Visits a Super expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitSuperExpr(Super expr);
    /**
     * Visits a This expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitThisExpr(This expr);
    /**
     * Visits a Unary expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitUnaryExpr(Unary expr);
    /**
     * Visits a Variable expr node.
     *
     * @param expr the node to visit
     * @return the visitor's result
     */
    R visitVariableExpr(Variable expr);
  }
  /**
   * Represents a Assign expr node.
   */
  static class Assign extends Expr {
    /**
     * Creates a new Assign node.
     *
     * @param name the name child/operand
     * @param value the value child/operand
     */
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    /** The name child/operand. */
    final Token name;
    /** The value child/operand. */
    final Expr value;
  }
  /**
   * Represents a Binary expr node.
   */
  static class Binary extends Expr {
    /**
     * Creates a new Binary node.
     *
     * @param left the left child/operand
     * @param operator the operator child/operand
     * @param right the right child/operand
     */
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    /** The left child/operand. */
    final Expr left;
    /** The operator child/operand. */
    final Token operator;
    /** The right child/operand. */
    final Expr right;
  }
  /**
   * Represents a Call expr node.
   */
  static class Call extends Expr {
    /**
     * Creates a new Call node.
     *
     * @param callee the callee child/operand
     * @param paren the paren child/operand
     * @param arguments the arguments child/operand
     */
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    /** The callee child/operand. */
    final Expr callee;
    /** The paren child/operand. */
    final Token paren;
    /** The arguments child/operand. */
    final List<Expr> arguments;
  }
  /**
   * Represents a Get expr node.
   */
  static class Get extends Expr {
    /**
     * Creates a new Get node.
     *
     * @param object the object child/operand
     * @param name the name child/operand
     */
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    /** The object child/operand. */
    final Expr object;
    /** The name child/operand. */
    final Token name;
  }
  /**
   * Represents a Grouping expr node.
   */
  static class Grouping extends Expr {
    /**
     * Creates a new Grouping node.
     *
     * @param expression the expression child/operand
     */
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    /** The expression child/operand. */
    final Expr expression;
  }
  /**
   * Represents a Literal expr node.
   */
  static class Literal extends Expr {
    /**
     * Creates a new Literal node.
     *
     * @param value the value child/operand
     */
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    /** The value child/operand. */
    final Object value;
  }
  /**
   * Represents a Logical expr node.
   */
  static class Logical extends Expr {
    /**
     * Creates a new Logical node.
     *
     * @param left the left child/operand
     * @param operator the operator child/operand
     * @param right the right child/operand
     */
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    /** The left child/operand. */
    final Expr left;
    /** The operator child/operand. */
    final Token operator;
    /** The right child/operand. */
    final Expr right;
  }
  /**
   * Represents a Set expr node.
   */
  static class Set extends Expr {
    /**
     * Creates a new Set node.
     *
     * @param object the object child/operand
     * @param name the name child/operand
     * @param value the value child/operand
     */
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }

    /** The object child/operand. */
    final Expr object;
    /** The name child/operand. */
    final Token name;
    /** The value child/operand. */
    final Expr value;
  }
  /**
   * Represents a Super expr node.
   */
  static class Super extends Expr {
    /**
     * Creates a new Super node.
     *
     * @param keyword the keyword child/operand
     * @param method the method child/operand
     */
    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSuperExpr(this);
    }

    /** The keyword child/operand. */
    final Token keyword;
    /** The method child/operand. */
    final Token method;
  }
  /**
   * Represents a This expr node.
   */
  static class This extends Expr {
    /**
     * Creates a new This node.
     *
     * @param keyword the keyword child/operand
     */
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }

    /** The keyword child/operand. */
    final Token keyword;
  }
  /**
   * Represents a Unary expr node.
   */
  static class Unary extends Expr {
    /**
     * Creates a new Unary node.
     *
     * @param operator the operator child/operand
     * @param right the right child/operand
     */
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    /** The operator child/operand. */
    final Token operator;
    /** The right child/operand. */
    final Expr right;
  }
  /**
   * Represents a Variable expr node.
   */
  static class Variable extends Expr {
    /**
     * Creates a new Variable node.
     *
     * @param name the name child/operand
     */
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    /** The name child/operand. */
    final Token name;
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
