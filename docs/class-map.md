# jlox Class Map

## Class Relationships

```mermaid
classDiagram
    class Lox {
        +main(String[] args)
        +run(String source)
        +error(int line, String message)
        +error(Token token, String message)
        +runtimeError(RuntimeError error)
        -runFile(String path)
        -runPrompt()
        -report(...)
    }

    class Main {
        +main(String[] args)
    }

    class Scanner {
        +scanTokens() List~Token~
    }

    class Parser {
        +parse() List~Stmt~
    }

    class Resolver {
        +resolve(List~Stmt~ stmts)
    }

    class Interpreter {
        +interpret(List~Stmt~ stmts)
        +resolve(Expr expr, int depth)
        +executeBlock(List~Stmt~, Environment)
    }

    class Environment {
        +define(String, Object)
        +get(Token) Object
        +assign(Token, Object)
        +getAt(int, String) Object
        +assignAt(int, Token, Object)
    }

    class Token {
        +TokenType type
        +String lexeme
        +Object literal
        +int line
    }

    class TokenType {
        <<enumeration>>
    }

    class RuntimeError {
        +Token token
    }

    class Return {
        +Object value
    }

    class LoxCallable {
        <<interface>>
        +arity() int
        +call(Interpreter, List~Object~) Object
    }

    class LoxFunction {
        +call(Interpreter, List~Object~) Object
        +bind(LoxInstance) LoxFunction
    }

    class LoxClass {
        +call(Interpreter, List~Object~) Object
        +findMethod(String) LoxFunction
    }

    class LoxInstance {
        +get(Token) Object
        +set(Token, Object)
    }

    class Expr {
        <<abstract>>
    }

    class Stmt {
        <<abstract>>
    }

    Main --> Lox : delegates
    Lox --> Scanner : creates
    Lox --> Parser : creates
    Lox --> Resolver : creates
    Lox --> Interpreter : owns static instance
    Scanner --> Token : produces
    Scanner --> TokenType : uses
    Parser --> Token : consumes
    Parser --> Stmt : produces
    Parser --> Expr : produces
    Resolver --> Expr : visits
    Resolver --> Stmt : visits
    Resolver --> Interpreter : records depths
    Interpreter --> Expr : visits
    Interpreter --> Stmt : visits
    Interpreter --> Environment : uses
    Interpreter --> LoxCallable : invokes
    Interpreter --> RuntimeError : throws
    Interpreter --> Return : catches
    LoxFunction ..|> LoxCallable
    LoxClass ..|> LoxCallable
    LoxFunction --> Environment : closure
    LoxClass --> LoxFunction : methods
    LoxClass --> LoxInstance : constructs
    LoxInstance --> LoxClass
    Environment --> Environment : enclosing
```

## Package Layout

```mermaid
flowchart LR
    subgraph com.lox
        Lox
        Main
        Scanner
        Parser
        Resolver
        Interpreter
        Environment
        Token
        TokenType
        Expr
        Stmt
        LoxCallable
        LoxFunction
        LoxClass
        LoxInstance
        RuntimeError
        Return
    end

    subgraph com.tool
        GenerateAst
    end

    com.tool --> com.lox : generates Expr.java, Stmt.java
```
