package com.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Code generator for the Lox AST classes.
 *
 * <p>This tool emits {@code Expr.java} and {@code Stmt.java} from a compact,
 * declarative description of each node type. Running it keeps the visitor
 * boilerplate in one place instead of maintaining it by hand.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 *   java com.tool.GenerateAst <output directory>
 * }</pre>
 *
 * <p>The generated files use the classic visitor pattern: each concrete node
 * implements {@code accept(Visitor<R>)} so interpreters, printers, and future
 * tools can walk the tree without modifying the node classes.</p>
 */
public class GenerateAst {

    /**
     * Entry point. Expects exactly one argument: the directory where
     * {@code Expr.java} and {@Stmt.java} should be written.
     *
     * @param args command-line arguments
     * @throws IOException if writing the generated files fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign    : Token name, Expr value",
                "Binary    : Expr left, Token operator, Expr right",
                "Call      : Expr callee, Token paren, List<Expr> arguments",
                "Get       : Expr object, Token name",
                "Grouping  : Expr expression",
                "Literal   : Object value",
                "Logical   : Expr left, Token operator, Expr right",
                "Set       : Expr object, Token name, Expr value",
                "Super     : Token keyword, Token method",
                "This      : Token keyword",
                "Unary     : Token operator, Expr right",
                "Variable  : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr cond, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "While      : Expr cond, Stmt body",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr init"
        ));
    }


    /**
     * Writes a single abstract base class and all of its concrete subclasses.
     *
     * @param outputDir directory for the generated file
     * @param baseName  name of the AST base class (e.g. "Expr" or "Stmt")
     * @param types     node descriptions in "ClassName : FieldType fieldName, ..." form
     * @throws IOException if the file cannot be written
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("/**");
        writer.println(" * Auto-generated abstract syntax tree base class for " + baseName.toLowerCase() + " nodes.");
        writer.println(" *");
        writer.println(" * <p>Do not edit by hand. Regenerate with {@code make ast}.</p>");
        writer.println(" */");
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  /**");
        writer.println("   * Dispatches this node to the appropriate method on {@code visitor}.");
        writer.println("   *");
        writer.println("   * @param visitor the visitor to invoke");
        writer.println("   * @param <R>     the visitor's result type");
        writer.println("   * @return the value produced by the visitor");
        writer.println("   */");
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    /**
     * Emits the {@code Visitor<R>} interface for the given AST base class.
     *
     * @param writer   the output writer
     * @param baseName name of the AST base class
     * @param types    node descriptions
     */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  /**");
        writer.println("   * Visitor interface for " + baseName + " nodes.");
        writer.println("   *");
        writer.println("   * @param <R> the type returned by every visit method");
        writer.println("   */");
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    /**");
            writer.println("     * Visits a " + typeName + " " + baseName.toLowerCase() + " node.");
            writer.println("     *");
            writer.println("     * @param " + baseName.toLowerCase() + " the node to visit");
            writer.println("     * @return the visitor's result");
            writer.println("     */");
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    /**
     * Emits one concrete subclass with its constructor, fields, and accept method.
     *
     * @param writer    the output writer
     * @param baseName  name of the AST base class
     * @param className name of the concrete subclass
     * @param fieldList comma-separated "Type name" field declarations
     */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  /**");
        writer.println("   * Represents a " + className + " " + baseName.toLowerCase() + " node.");
        writer.println("   */");
        writer.println("  static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println("    /**");
        writer.println("     * Creates a new " + className + " node.");
        writer.println("     *");
        for (String field : fieldList.split(", ")) {
            String name = field.split(" ")[1];
            writer.println("     * @param " + name + " the " + name + " child/operand");
        }
        writer.println("     */");
        writer.println("    " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // Visitor pattern implementation
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // Fields
        writer.println();
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("    /** The " + name + " child/operand. */");
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
