package com.lox;

/**
 * A single lexical token produced by the {@link Scanner}.
 *
 * <p>Each token records the token type, the raw lexeme as it appeared in the
 * source, an optional literal value (for numbers and strings), and the source
 * line on which the token occurs.</p>
 */
class Token {
    /** The kind of token (keyword, operator, literal, etc.). */
    final TokenType type;

    /** The raw text of the token as it appeared in the source. */
    final String lexeme;

    /** The literal value for number and string tokens; otherwise {@code null}. */
    final Object literal;

    /** The 1-based source line where the token appears. */
    final int line;

    /**
     * Creates a new token.
     *
     * @param type    the token type
     * @param lexeme  the token's source text
     * @param literal the literal value, or {@code null} if none
     * @param line    the source line of the token
     */
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * Returns a debug representation of the token.
     *
     * @return "TYPE lexeme literal"
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
