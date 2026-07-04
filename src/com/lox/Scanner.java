package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.TokenType.*;

/**
 * Lexical scanner (tokenizer) for Lox.
 *
 * <p>Transforms a raw string of source code into a list of {@link Token}s.
 * The scanner recognizes punctuation, operators, number and string literals,
 * identifiers, and reserved keywords. It skips whitespace and comments and
 * tracks line numbers for error reporting.</p>
 */
class Scanner {
    /** The raw source code being scanned. */
    private final String source;

    /** The tokens produced so far. */
    private final List<Token> tokens = new ArrayList<>();

    /** Index of the first character of the token currently being scanned. */
    private int start = 0;

    /** Index of the character currently being considered. */
    private int curr = 0;

    /** Current 1-based source line. */
    private int line = 1;

    /** Map from reserved keyword lexemes to their token types. */
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    /**
     * Creates a scanner for the given source code.
     *
     * @param source the Lox source to tokenize
     */
    Scanner(String source) {
        this.source = source;
    }

    /**
     * Scans the entire source string into a list of tokens.
     *
     * <p>An {@link TokenType#EOF} token is appended at the end so the parser
     * always has a boundary marker.</p>
     *
     * @return the list of scanned tokens
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = curr;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Scans a single token starting at {@link #curr}.
     *
     * <p>This is the main dispatch routine: it consumes one character and
     * decides what kind of token to emit, handling multi-character operators,
     * comments, and literals.</p>
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                // Ignore insignificant whitespace.
                break;
            case '\n':
                line++;
                break;
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '?':
                addToken(QUESTION);
                break;
            case ':':
                addToken(COLON);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESSER_EQUAL : LESSER);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A line comment runs until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character '" + c + "'.");
                    break;
                }
        }
    }


    /**
     * Scans a string literal.
     *
     * <p>Handles multi-line strings and reports an error if the string is not
     * closed before the end of the source.</p>
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string literal.");
            return;
        }

        // Consume the closing quote.
        advance();

        String value = source.substring(start + 1, curr - 1);
        addToken(STRING, value);
    }

    /**
     * Scans an identifier or keyword.
     *
     * <p>After consuming the identifier, its lexeme is looked up in
     * {@link #keywords} to decide whether it is a reserved word.</p>
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, curr);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    /**
     * Scans a numeric literal.
     *
     * <p>Supports integers and decimal fractions. The literal is stored as a
     * {@link Double}.</p>
     */
    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the fractional part.
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, curr)));
    }

    /**
     * Returns {@code true} if {@code c} is an ASCII digit.
     *
     * @param c the character to test
     * @return whether {@code c} is a digit
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Returns {@code true} if {@code c} is an ASCII letter or underscore.
     *
     * @param c the character to test
     * @return whether {@code c} can start an identifier
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }

    /**
     * Returns {@code true} if {@code c} is alphanumeric or underscore.
     *
     * @param c the character to test
     * @return whether {@code c} can continue an identifier
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Conditionally consumes {@code expected} if it is the next character.
     *
     * @param expected the character to match
     * @return {@code true} if the character was consumed
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(curr) != expected) return false;

        curr++;
        return true;
    }

    /**
     * Returns the current character without consuming it.
     *
     * @return the current character, or {@code '\0'} at end of input
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(curr);
    }

    /**
     * Returns the character after the current one without consuming anything.
     *
     * @return the next character, or {@code '\0'} if near end of input
     */
    private char peekNext() {
        if (curr + 1 >= source.length()) return '\0';
        return source.charAt(curr + 1);
    }

    /**
     * Consumes and returns the current character.
     *
     * @return the consumed character
     */
    private char advance() {
        return source.charAt(curr++);
    }

    /**
     * Adds a token with no literal value.
     *
     * @param type the token type to add
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adds a token with a literal value.
     *
     * @param type    the token type to add
     * @param literal the literal value, or {@code null}
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, curr);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Returns {@code true} when the entire source has been consumed.
     *
     * @return whether scanning has reached the end of input
     */
    private boolean isAtEnd() {
        return curr >= source.length();
    }
}
