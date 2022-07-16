/*
 * File: Lexer.java
 * Date: Spring 2022
 * Auth: Katie Stevens
 * Desc:
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public class Lexer {

  private BufferedReader buffer; // handle to input stream
  private int line = 1;          // current line number
  private int column = 0;        // current column number


  //--------------------------------------------------------------------
  // Constructor
  //--------------------------------------------------------------------
  
  public Lexer(InputStream instream) {
    buffer = new BufferedReader(new InputStreamReader(instream));
  }


  //--------------------------------------------------------------------
  // Private helper methods
  //--------------------------------------------------------------------

  // Returns next character in the stream. Returns -1 if end of file.
  private int read() throws MyPLException {
    try {
      return buffer.read();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return -1;
  }

  
  // Returns next character without removing it from the stream.
  private int peek() throws MyPLException {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = read();
      buffer.reset();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return ch;
  }


  // Print an error message and exit the program.
  private void error(String msg, int line, int column) throws MyPLException {
    msg = msg + " at line " + line + ", column " + column;
    throw MyPLException.LexerError(msg);
  }

  
  // Checks for whitespace 
  public static boolean isWhitespace(int ch) {
    return Character.isWhitespace((char)ch);
  }

  
  // Checks for digit
  private static boolean isDigit(int ch) {
    return Character.isDigit((char)ch);
  }

  
  // Checks for letter
  private static boolean isLetter(int ch) {
    return Character.isLetter((char)ch);
  }

  
  // Checks if given symbol
  private static boolean isSymbol(int ch, char symbol) {
    return (char)ch == symbol;
  }

  
  // Checks if end-of-file
  private static boolean isEOF(int ch) {
    return ch == -1;
  }
  

  //--------------------------------------------------------------------
  // Public next_token function
  //--------------------------------------------------------------------
  
  // Returns next token in input stream
  public Token nextToken() throws MyPLException {
    // TODO: implement nextToken()

    // Note: Use the error() function to report errors, e.g.:
    //         error("error msg goes here", line, column);

    // Reminder: Comment your code and fill in the file header comment
    // above
    
    int curr = read();
    column++;
    String tmp = "";

    while (isWhitespace(curr)) {
      if (isSymbol(curr, '\n')) {
        line++;
        column = 1;
      }
      else {
        column++;
      }
      curr = read();
    }

    if (isSymbol(curr, '#')) { // check for comment
      // skip to new line
      while (isSymbol(curr, '\n')) {
        curr = read();
      }
      line++;
      column = 1;
    }

    // check for end of file
    if (isEOF(curr)) { 
      return new Token(TokenType.EOS, "end-of-file", line, column);
    }

    // check for single char tokens
    if (isSymbol(curr, ',')) {
      return new Token(TokenType.COMMA, ",", line, column);
    }
    else if (isSymbol(curr, '.')) {
      return new Token(TokenType.DOT, ".", line, column);
    }
    else if (isSymbol(curr, '+')) {
      return new Token(TokenType.PLUS, "+", line, column);
    }
    else if (isSymbol(curr, '-')) {
      return new Token(TokenType.MINUS, "-", line, column);
    }
    else if (isSymbol(curr, '*')) {
      return new Token(TokenType.MULTIPLY, "*", line, column);
    }
    else if (isSymbol(curr, '/')) {
      return new Token(TokenType.DIVIDE, "/", line, column);
    }
    else if (isSymbol(curr, '%')) {
      return new Token(TokenType.MODULO, "%", line, column);
    }
    else if (isSymbol(curr, '{')) {
      return new Token(TokenType.LBRACE, "{", line, column);
    }
    else if (isSymbol(curr, '}')) {
      return new Token(TokenType.RBRACE, "}", line, column);
    }
    else if (isSymbol(curr, '(')) {
      return new Token(TokenType.LPAREN, "(", line, column);
    }
    else if (isSymbol(curr, ')')) {
      return new Token(TokenType.RPAREN, ")", line, column);
    }
    else if (isSymbol(curr, '>')) {
      if (isSymbol(peek(), '=')) {
        curr = read();
        column++;
        return new Token(TokenType.GREATER_THAN_EQUAL, ">=", line, column - 1);
      }
      else {
        return new Token(TokenType.GREATER_THAN, ">", line, column);
      }
    }
    else if (isSymbol(curr, '<')) {
      if (isSymbol(peek(), '=')) {
        curr = read();
        column++;
        return new Token(TokenType.LESS_THAN_EQUAL, "<=", line, column - 1);
      }
      else {
        return new Token(TokenType.LESS_THAN, "<", line, column);
      }
    }
    else if (isSymbol(curr, '!')) {
      if (isSymbol(peek(), '=')) {
        curr = read();
        column++;
        return new Token(TokenType.NOT_EQUAL, "!=", line, column - 1);
      }
      else if (isWhitespace(peek()) || isLetter(peek())) {
        return new Token(TokenType.NOT, "!", line, column);
      }
      else {
        error("expecting '=', found '" + (char)peek() + "'", line, column + 1);
      }
    }
    else if (isSymbol(curr, '=')) {
      if (isSymbol(peek(), '=')) {
        curr = read();
        column++;
        return new Token(TokenType.EQUAL, "==", line, column - 1);
      }
      else {
        return new Token(TokenType.ASSIGN, "=", line, column);
      }
    }
    
    // characters
    else if (isSymbol(curr, '\'')) {
      if (isSymbol(peek(), '\'')) {
        error("empty character", line, column);
      }
      curr = read();
      if (isSymbol(peek(), '\'')) { // char
        tmp = "" + (char)curr;
        curr = read();
        column = column + 2;
        return new Token(TokenType.CHAR_VAL, tmp, line, column - 2);
      }
      else if (isSymbol(curr, '\\')){
        curr = read();
        if (isSymbol(curr, 'n')) {
          if (isSymbol(peek(), '\'')) {
            column += 3;
            curr = read();
            return new Token(TokenType.CHAR_VAL, "\\n", line, column - 3);
          }
          else {
            error("expecting ' found, '" + (char)peek() + "'", line, column + 1);
          }
        }
        else if (isSymbol(curr, 't')) {
          if (isSymbol(peek(), '\'')) {
            column += 3;
            curr = read();
            return new Token(TokenType.CHAR_VAL, "\\t", line, column - 3);
          }
          else {
            error("expecting ' found, '" + (char)peek() + "'", line, column + 1);
          }
        }
        else {
          error("expecting n or t found, " + (char)peek(), line, column + 1); 
        }
      }
      else {
        error("expecting ' found, '" + (char)peek() + "'", line, column + 1);
      }
    }

    // strings
    else if (isSymbol(curr, '\"')) {
      curr = read();
      while (!isSymbol(curr, '\"')) {
        if (curr == -1) {
          error("found end-of-file in string", line, column + tmp.length());
        }
        if (isSymbol(curr, '\n')) {
          error("found newline within string", line, column + 1);
        }
        tmp += (char)curr;
        curr = read();
      }
      int offset = tmp.length() + 1;
      column += offset;
      return new Token(TokenType.STRING_VAL, tmp, line, column - offset);
    }

    // digits
    else if (isDigit(curr)) {
      boolean isDouble = false;
      tmp += (char)curr;
      
      while(isDigit(peek()) || isSymbol(peek(), '.')) {
        curr = read();
        if (isSymbol(curr, '.')) {
          if (isDouble) {
            error("too many decimal points in double value \'" + tmp + "\'", line, column);
          }
          else {
            isDouble = true;
          }
        }
        tmp += (char)curr;
      }
      int offset = tmp.length() - 1;
      column += offset;

      if (isSymbol(peek(), ' ') || isSymbol(peek(), '\n') || peek() == -1) {
        if (isDouble) { // double
          if (tmp.charAt(0) == '0' && tmp.charAt(1) != '.') {
            error("leading zero in '" + tmp + "'", line, column - offset);
          }
          return new Token(TokenType.DOUBLE_VAL, tmp, line, column - offset);
        }
        else { // int
          if (tmp.charAt(0) == '0' && tmp.length() > 1) {
            error("leading zero in '" + tmp + "'", line, column - offset);
          }
          return new Token(TokenType.INT_VAL, tmp, line, column - offset);
        }
      }
      else { 
        error("missing decimal digit in double value '" + tmp + "'", line, column - offset);
      }
    }

    // reserved words
    else if (isLetter(curr)) {
      tmp += (char)curr;
      while (isLetter(peek()) || isDigit(peek()) || isSymbol(peek(), '_')) {
        curr = read();
        tmp += (char)curr;
      }
      int offset = tmp.length() - 1;
      column += offset;
      //boolean operators
      if (tmp.equals("and")) {
        return new Token(TokenType.AND, "and", line, column - offset);
      }
      else if (tmp.equals("or")) {
        return new Token(TokenType.OR, "or", line, column - offset);
      }
      else if (tmp.equals("not")) {
        return new Token(TokenType.NOT, "not", line, column - offset);
      }
      else if (tmp.equals("neg")) {
        return new Token(TokenType.NEG, "neg", line, column - offset);
      }
      else if (tmp.equals("true")) {
        return new Token(TokenType.BOOL_VAL, "true", line, column - offset);
      }
      else if (tmp.equals("false")) {
        return new Token(TokenType.BOOL_VAL, "false", line, column - offset);
      }
      // data types
      else if (tmp.equals("int")) {
        return new Token(TokenType.INT_TYPE, "int", line, column - offset);
      }
      else if (tmp.equals("double")) {
        return new Token(TokenType.DOUBLE_TYPE, "double", line, column - offset);
      }
      else if (tmp.equals("char")) {
        return new Token(TokenType.CHAR_TYPE, "char", line, column - offset);
      }
      else if (tmp.equals("string")) {
        return new Token(TokenType.STRING_TYPE, "string", line, column - offset);
      }
      else if (tmp.equals("bool")) {
        return new Token(TokenType.BOOL_TYPE, "bool", line, column - offset);
      }
      else if (tmp.equals("void")) {
        return new Token(TokenType.VOID_TYPE, "void", line, column - offset);
      }
      // reserved words
      else if (tmp.equals("var")) {
        return new Token(TokenType.VAR, "var", line, column - offset);
      }
      else if (tmp.equals("type")) {
        return new Token(TokenType.TYPE, "type", line, column - offset);
      }
      else if (tmp.equals("while")) {
        return new Token(TokenType.WHILE, "while", line, column - offset);
      }
      else if (tmp.equals("for")) {
        return new Token(TokenType.FOR, "for",  line, column - offset);
      }
      else if (tmp.equals("from")) {
        return new Token(TokenType.FROM, "from", line, column - offset);
      }
      else if (tmp.equals("upto")) {
        return new Token(TokenType.UPTO, "upto", line, column - offset);
      }
      else if (tmp.equals("downto")) {
        return new Token(TokenType.DOWNTO, "downto", line, column - offset);
      }
      else if (tmp.equals("if")) {
        return new Token(TokenType.IF, "if", line, column - offset);
      }
      else if (tmp.equals("elif")) {
        return new Token(TokenType.ELIF, "elif", line, column - offset);
      }
      else if (tmp.equals("else")) {
        return new Token(TokenType.ELSE, "else", line, column - offset);
      }
      else if (tmp.equals("fun")) {
        return new Token(TokenType.FUN, "fun", line, column - offset);
      }
      else if (tmp.equals("new")) {
        return new Token(TokenType.NEW, "new", line, column - offset);
      }
      else if (tmp.equals("delete")) {
        return new Token(TokenType.DELETE, "delete", line, column - offset);
      }
      else if(tmp.equals("return")) {
        return new Token(TokenType.RETURN, "return", line, column - offset);
      }
      else if(tmp.equals("nil")) {
        return new Token(TokenType.NIL, "nil", line, column - offset);
      }
      // identifiers
      else {
        return new Token(TokenType.ID, tmp, line, column - offset);
      }
    }
    else {
      error("invalid symbol '" + (char)curr + "'", line, column);
    }
  


    // The following returns the end-of-file token, you'll need to
    // remove this to implement nextToken (it is only here so that the
    // program can compile initially)
    return new Token(TokenType.EOS, "end-of-file", line, column);
  }

}
