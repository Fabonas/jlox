package com.lox;

/**
 * Enumerates every kind of token that the {@link Scanner} can produce.
 *
 * <p>The tokens are grouped by shape: single-character punctuation,
 * one-or-two-character operators, literal tokens, keywords, and the special
 * end-of-file marker.</p>
 */
public enum TokenType {
    // Single-char tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, QUESTION, COLON,

    // One or two characters
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESSER, LESSER_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE, BREAK,

    // End-of-file
    EOF
}
