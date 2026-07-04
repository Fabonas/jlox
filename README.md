# jlox

A complete, tree-walking interpreter for the **Lox** programming language, implemented in Java. This project follows Part II of [Crafting Interpreters](https://craftinginterpreters.com/) by Bob Nystrom.

Lox is a small dynamically-typed scripting language with C-style syntax, first-class functions, closures, and familiar imperative control flow. This implementation also adds object-oriented programming with classes, methods, fields, and constructors.

---

## Table of contents

- [What is this?](#what-is-this)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Project layout](#project-layout)
- [Architecture](#architecture)
- [Regenerating the AST classes](#regenerating-the-ast-classes)
- [Language grammar](#language-grammar)
- [Examples](#examples)
- [Error handling](#error-handling)
- [What I learned](#what-i-learned)
- [License](#license)

---

## What is this?

This is a from-scratch interpreter written while working through *Crafting Interpreters*. It takes Lox source code, tokenizes it, parses it into an abstract syntax tree, resolves variable scopes, and then evaluates it with a tree-walking interpreter.

The project is intentionally educational: the code is organized to mirror the book's chapters and shows how a real programming language frontend and runtime fit together.

---

## Features

- **Arithmetic and logic:** `+`, `-`, `*`, `/`, comparison, equality, logical `and` / `or`, unary `!` and `-`.
- **Variables and scopes:** `var` declarations, blocks, nested scopes, and shadowing.
- **Control flow:** `if` / `else`, `while`, and C-style `for` loops.
- **Functions:** user-defined functions, recursion, first-class functions, closures, and a native `clock()` function.
- **Classes and objects:** class declarations, methods, fields, constructors named `init`, and `this` for method access.
- **Execution modes:** run a script file or use the interactive REPL.
- **Error reporting:** clear compile-time and runtime error messages with source line numbers.

---

## Prerequisites

- A JDK, Java 8 or later.
- `make` (optional but recommended).

No external libraries or build tools are required.

---

## Quick start

```bash
# Clone or enter the project directory
cd jlox

# Compile everything
make

# Start the interactive REPL
make run

# Run a Lox script
make run f=examples/hello.lox

# Regenerate Expr.java and Stmt.java from the AST description
make ast

# Show all available Makefile targets
make help

# Remove compiled classes
make clean
```

---

## Project layout

```text
.
├── Makefile                 # Build, run, and AST generation targets
├── README.md                # This file
├── jlox.iml                 # IntelliJ IDEA module file
├── src/
│   ├── com/tool/
│   │   └── GenerateAst.java # Code generator for Expr.java / Stmt.java
│   └── com/lox/
│       ├── Lox.java         # Entry point, REPL, error reporting
│       ├── Main.java        # Alternative entry point (delegates to Lox)
│       ├── Scanner.java     # Lexer: source → tokens
│       ├── Token.java       # A single lexical token
│       ├── TokenType.java   # Token kinds
│       ├── Parser.java      # Recursive-descent parser: tokens → AST
│       ├── Expr.java        # Generated expression AST nodes
│       ├── Stmt.java        # Generated statement AST nodes
│       ├── Interpreter.java # Tree-walking evaluator
│       ├── Environment.java # Variable storage / scope chain
│       ├── Resolver.java    # Static scope resolver
│       ├── LoxCallable.java # Interface for callables
│       ├── LoxFunction.java # User-defined function object
│       ├── LoxClass.java    # Runtime class object
│       ├── LoxInstance.java # Runtime instance object
│       ├── FunctionType.java# Context the resolver tracks for functions
│       ├── Return.java      # Unwinding exception for return values
│       └── RuntimeError.java# Runtime error with offending token
```

---

## Architecture

jlox processes source code in four phases:

```text
Lox source
    │
    ▼
+-----------+
│  Scanner  │  ← characters → tokens
+-----------+
    │
    ▼
+-----------+
│  Parser   │  ← tokens → AST (Expr / Stmt)
+-----------+
    │
    ▼
+-----------+
│ Resolver  │  ← resolves variable scopes, reports invalid returns
+-----------+
    │
    ▼
+-----------+
│ Interpreter│ ← evaluates the AST
+-----------+
```

1. **Scanner** walks the raw source string and produces a flat list of `Token` objects. It skips whitespace and comments, recognizes keywords, and tracks line numbers.
2. **Parser** builds a tree of `Expr` and `Stmt` nodes using recursive descent. Each grammar rule maps to one method. `for` loops are desugared into `while` loops plus blocks.
3. **Resolver** performs a single static pass over the AST. It records how many scopes deep each variable reference is, catches uses of `return` outside functions, and detects invalid self-references in initializers.
4. **Interpreter** walks the resolved tree and executes it. It uses an `Environment` chain for variable storage and Java exceptions to unwind `return` statements.

### Visitors

`Expr` and `Stmt` are generated by `GenerateAst` and use the classic visitor pattern. The parser produces nodes, then the resolver and interpreter each implement the generated `Visitor` interfaces to walk the tree.

---

## Regenerating the AST classes

`Expr.java` and `Stmt.java` are generated from the node descriptions in `GenerateAst.java`. If you add a new expression or statement type, update the lists in `GenerateAst` and run:

```bash
make ast
```

This rewrites `src/com/lox/Expr.java` and `src/com/lox/Stmt.java`. Do not edit those two files by hand; they will be overwritten.

---

## Language grammar

This is an informal EBNF summary of the grammar supported by jlox.

```ebnf
program        → declaration* EOF ;

declaration    → classDecl
               | funDecl
               | varDecl
               | statement ;

classDecl      → "class" IDENTIFIER "{" method* "}" ;
method         → IDENTIFIER "(" parameters? ")" block ;

funDecl        → "fun" function ;
function       → IDENTIFIER "(" parameters? ")" block ;
parameters     → IDENTIFIER ( "," IDENTIFIER )* ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                            expression? ";"
                            expression? ")" statement ;
ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
printStmt      → "print" expression ";" ;
returnStmt     → "return" expression? ";" ;
whileStmt      → "while" "(" expression ")" statement ;
block          → "{" declaration* "}" ;
exprStmt       → expression ";" ;

expression     → assignment ;
assignment     → ( call "." )? IDENTIFIER "=" assignment
               | logic_or ;
logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | call ;
call           → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
arguments      → expression ( "," expression )* ;
primary        → "true" | "false" | "nil" | "this"
               | NUMBER | STRING | IDENTIFIER
               | "(" expression ")" ;
```

---

## Examples

### Hello world

```lox
print "Hello, jlox!";
```

Run it with:

```bash
make run f=examples/hello.lox
```

### Fibonacci with timing

```lox
fun fib(n) {
    if (n <= 1) return n;
    return fib(n - 2) + fib(n - 1);
}

var start = clock();
print fib(10);
print clock() - start;
```

Run it with:

```bash
make run f=examples/fib.lox
```

### Classes and objects

```lox
class Cake {
    init(flavor) {
        this.flavor = flavor;
    }

    describe() {
        print "A delicious " + this.flavor + " cake.";
    }
}

var cake = Cake("chocolate");
cake.describe();
```

Run it with:

```bash
make run f=examples/class.lox
```

---

## Error handling

Compile-time errors (scanning, parsing, or resolution) are printed to standard error with a source line number and a short message. After a compile-time error, jlox exits with status `65` when running a script.

Runtime errors (for example, adding a number to a string, dividing by zero, or accessing an undefined property) are reported similarly and cause an exit status of `70` in script mode. The REPL resets its error state after every line so a single mistake does not end the session.

---

## What I learned

Building jlox from scratch gave me hands-on experience with:

- Lexical analysis and tokenization.
- Recursive-descent parsing and abstract syntax trees.
- Static analysis with a variable resolver.
- Tree-walking interpreters and runtime value representation.
- Scope chains, closures, and first-class functions.
- Classes, instances, methods, and constructors in a dynamic language.
- Clean separation of concerns between scanner, parser, resolver, and interpreter.

---

## License

This is an educational implementation. Refer to the original [Crafting Interpreters](https://craftinginterpreters.com/) text for its license and attribution terms.
