/* 
 * File: Parser.java
 * Date: Spring 2022
 * Auth: Katie Stevens
 * Desc: Parser HW #3
 */


public class Parser {

  private Lexer lexer = null; 
  private Token currToken = null;
  private final boolean DEBUG = false;

  
  // constructor
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  // do the parse
  public void parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    advance();
    while (!match(TokenType.EOS)) {
      if (match(TokenType.TYPE))
        tdecl();
      else
        fdecl();
    }
    advance(); // eat the EOS token
  }

  
  //------------------------------------------------------------ 
  // Helper Functions
  //------------------------------------------------------------

  // get next token
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // advance if current token is of given type, otherwise error
  private void eat(TokenType t, String msg) throws MyPLException {
    if (match(t))
      advance();
    else
      error(msg);
  }

  // true if current token is of type t
  private boolean match(TokenType t) {
    return currToken.type() == t;
  }
  
  // throw a formatted parser error
  private void error(String msg) throws MyPLException {
    String s = msg + ", found '" + currToken.lexeme() + "' ";
    s += "at line " + currToken.line();
    s += ", column " + currToken.column();
    throw MyPLException.ParseError(s);
  }

  // output a debug message (if DEBUG is set)
  private void debug(String msg) {
    if (DEBUG)
      System.out.println("[debug]: " + msg);
  }

  // return true if current token is a (non-id) primitive type
  private boolean isPrimitiveType() {
    return match(TokenType.INT_TYPE) || match(TokenType.DOUBLE_TYPE) ||
      match(TokenType.BOOL_TYPE) || match(TokenType.CHAR_TYPE) ||
      match(TokenType.STRING_TYPE);
  }

  // return true if current token is a (non-id) primitive value
  private boolean isPrimitiveValue() {
    return match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
      match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
      match(TokenType.STRING_VAL);
  }
    
  // return true if current token starts an expression
  private boolean isExpr() {
    return match(TokenType.NOT) || match(TokenType.LPAREN) ||
      match(TokenType.NIL) || match(TokenType.NEW) ||
      match(TokenType.ID) || match(TokenType.NEG) ||
      match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
      match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
      match(TokenType.STRING_VAL);
  }

  private boolean isOperator() {
    return match(TokenType.PLUS) || match(TokenType.MINUS) ||
      match(TokenType.DIVIDE) || match(TokenType.MULTIPLY) ||
      match(TokenType.MODULO) || match(TokenType.AND) ||
      match(TokenType.OR) || match(TokenType.EQUAL) ||
      match(TokenType.LESS_THAN) || match(TokenType.GREATER_THAN) ||
      match(TokenType.LESS_THAN_EQUAL) || match(TokenType.GREATER_THAN_EQUAL) ||
      match(TokenType.NOT_EQUAL);
  }

  
  //------------------------------------------------------------
  // Recursive Descent Functions 
  //------------------------------------------------------------


  /* TODO: Add the recursive descent functions below */
  private void tdecl() throws MyPLException {
    eat(TokenType.TYPE, "Expecting type");
    eat(TokenType.ID, "Expecting id");
    eat(TokenType.LBRACE, "Expecting '{");
    // <vdecls>
    while (match(TokenType.VAR)) {
      vdecl_stmt();
    }
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void fdecl() throws MyPLException {
    eat(TokenType.FUN, "Expecting fun");
    // <dtype> | VOID
    if (match(TokenType.VOID_TYPE)) {
      eat(TokenType.VOID_TYPE, "Expecting void");
    } else {
      dtype();
    }
    eat(TokenType.ID, "Expecting ID");
    eat(TokenType.LPAREN, "Expecting '('");
    params();
    eat(TokenType.RPAREN, "Expecting ')'");   
    eat(TokenType.LBRACE, "Expecting '{");
    stmts();
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void vdecl_stmt() throws MyPLException {
    eat(TokenType.VAR, "Expecting var");
    if (isPrimitiveType() || match(TokenType.ID)) {
      dtype();
    }
    if (match(TokenType.ID)) {
      eat(TokenType.ID, "Expecting id");
    }
    eat(TokenType.ASSIGN, "Expecting '='");
    expr();
  }

  private void dtype() throws MyPLException {
    if (match(TokenType.INT_TYPE)) {
      eat(TokenType.INT_TYPE, "Expecting int");
    }
    else if (match(TokenType.DOUBLE_TYPE)) {
      eat(TokenType.DOUBLE_TYPE, "Expecting double");
    }
    else if (match(TokenType.BOOL_TYPE)) {
      eat(TokenType.BOOL_TYPE, "Expecting bool");
    }
    else if (match(TokenType.CHAR_TYPE)) {
      eat(TokenType.CHAR_TYPE, "Expecting char");
    }
    else if(match(TokenType.STRING_TYPE)) {
      eat(TokenType.STRING_TYPE, "Expecting string");
    }
    else {
      eat(TokenType.ID, "Expecting id");
    }
  }

  private void params() throws MyPLException {
    if (!match(TokenType.RPAREN)) {
      dtype();
      eat(TokenType.ID, "Expecting id");
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "Expecting ','");
        dtype();
        eat(TokenType.ID, "Expecting id");
      }
    }
  }

  private void expr() throws MyPLException {
    if (match(TokenType.NOT)) {
      eat(TokenType.NOT, "Expecting not");
      expr();
    }
    else if (match(TokenType.LPAREN)) {
      eat(TokenType.LPAREN, "Expecting '('");
      expr();
      eat(TokenType.RPAREN, "Expecting ')'");
    }
    else { // <rvalue>
      rvalue();
    }

    if (isOperator()) {
      operator();
      expr();
    }
  }

  private void rvalue() throws MyPLException {
    if (isPrimitiveValue()) {
      pval();
    }
    else if (match(TokenType.NIL)) {
      eat(TokenType.NIL, "Expecting nil");
    }
    else if (match(TokenType.NEW)) {
      eat(TokenType.NEW, "Expecting new");
      eat(TokenType.ID, "Expecting id");
    }
    else if (match(TokenType.ID)) {
      eat(TokenType.ID, "Expecting id");
      if (match(TokenType.LPAREN)) {
        call_expr();
      }
      else {
        idrval();
      }
    }
    else if (match(TokenType.NEG)) {
      eat(TokenType.NEG, "Expecting neg");
      expr();
    }
  }

  private void pval() throws MyPLException {
    if (match(TokenType.INT_VAL)) {
      eat(TokenType.INT_VAL, "Expecting int_val");
    }
    else if (match(TokenType.DOUBLE_VAL)) {
      eat(TokenType.DOUBLE_VAL, "Expecting double_val");
    }
    else if (match(TokenType.BOOL_VAL)) {
      eat(TokenType.BOOL_VAL, "Expecting bool_val");
    }
    else if (match(TokenType.CHAR_VAL)) {
      eat(TokenType.CHAR_VAL, "Expecting char_val");
    }
    else if (match(TokenType.STRING_VAL)) {
      eat(TokenType.STRING_VAL, "Expecting string_val");
    }
    else {
      error("Expected primitive value");
    }
  }

  private void call_expr() throws MyPLException {
    //eat(TokenType.ID, "Expecting id");
    eat(TokenType.LPAREN, "Expecting '('");
    args();
    eat(TokenType.RPAREN, "Expecting ')'");
  }

  private void idrval() throws MyPLException {
    //eat(TokenType.ID, "Expecting id");
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expecting '.'");
      eat(TokenType.ID, "Expecting id");
    }
  }

  private void args() throws MyPLException {
    if (isExpr()) {
      expr();
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "Expecting ','");
        expr();
      }
    }
  }

  private void operator() throws MyPLException {
    if (match(TokenType.PLUS)) {
      eat(TokenType.PLUS, "Expecting '+'");
    }
    else if (match(TokenType.MINUS)) {
      eat(TokenType.MINUS, "Expecting '-'");
    }
    else if (match(TokenType.DIVIDE)) {
      eat(TokenType.DIVIDE, "Expecting '/'");
    }
    else if (match(TokenType.MULTIPLY)) {
      eat(TokenType.MULTIPLY, "Expecting '*'");
    }
    else if (match(TokenType.MODULO)) {
      eat(TokenType.MODULO, "Expecting '%'");
    }
    else if (match(TokenType.AND)) {
      eat(TokenType.AND, "Expecting '&'");
    }
    else if (match(TokenType.OR)) {
      eat(TokenType.OR, "Expecting '|'");
    }
    else if (match(TokenType.EQUAL)) {
      eat(TokenType.EQUAL, "Expecting '=='");
    }
    else if (match(TokenType.LESS_THAN)) {
      eat(TokenType.LESS_THAN, "Expecting '<'");
    }
    else if (match(TokenType.GREATER_THAN)) {
      eat(TokenType.GREATER_THAN, "Expecting '>'");
    }
    else if (match(TokenType.LESS_THAN_EQUAL)) {
      eat(TokenType.LESS_THAN_EQUAL, "Expecting '<='");
    }
    else if (match(TokenType.GREATER_THAN_EQUAL)) {
      eat(TokenType.GREATER_THAN_EQUAL, "Expecting '>='");
    }
    else if (match(TokenType.NOT_EQUAL)) {
      eat(TokenType.NOT_EQUAL, "Expecting '!='");
    }
    else {
      error("Operator not found");
    }
  }

  private void stmts() throws MyPLException {
    while (match(TokenType.VAR) || match(TokenType.ID) || match(TokenType.IF) ||
           match(TokenType.WHILE) || match(TokenType.FOR) || match(TokenType.RETURN) ||
           match(TokenType.DELETE)) {
      stmt();
    }
  }

  private void stmt() throws MyPLException {
    if (match(TokenType.VAR)) {
      vdecl_stmt();
    }
    else if (match(TokenType.ID)) {
      eat(TokenType.ID, "Expecting id");
      if (match(TokenType.LPAREN)) {
        call_expr();
      }
      else {
        assign_stmt();
      }
    }
    else if (match(TokenType.IF)) {
      cond_stmt();
    }
    else if (match(TokenType.WHILE)) {
      while_stmt();
    }
    else if (match(TokenType.FOR)) {
      for_stmt();
    }
    else if (match(TokenType.RETURN)) {
      ret_stmt();
    }
    else if (match(TokenType.DELETE)) {
      delete_stmt();
    }
  }

  private void assign_stmt() throws MyPLException {
    //eat(TokenType.ID, "Expecting id");
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expecting '.'");
      eat(TokenType.ID, "Expecting id");
    }
    eat(TokenType.ASSIGN, "Expecting '='");
    expr();
  }

  private void cond_stmt() throws MyPLException {
    eat(TokenType.IF, "Expecting if");
    expr();
    eat(TokenType.LBRACE, "Expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "Expecting '}'");
    condt();
  }

  private void condt() throws MyPLException {
    if (match(TokenType.ELIF)) {
      eat(TokenType.ELIF, "Expecting elif");
      expr();
      eat(TokenType.LBRACE, "Expecting '{'");
      stmts();
      eat(TokenType.RBRACE, "Expecting '}'");
      condt();
    }
    else if (match(TokenType.ELSE)) {
      eat(TokenType.ELSE, "Expecting else");
      eat(TokenType.LBRACE, "Expecting '{'");
      stmts();
      eat(TokenType.RBRACE, "Expecting '}'");
    }
  }

  private void while_stmt() throws MyPLException {
    eat(TokenType.WHILE, "Expecting while");
    expr();
    eat(TokenType.LBRACE, "Expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void for_stmt() throws MyPLException {
    eat(TokenType.FOR, "Expecting for");
    eat(TokenType.ID, "Expecting id");
    eat(TokenType.FROM, "Expecting from");
    expr();
    if (match(TokenType.UPTO)) {
      eat(TokenType.UPTO, "Expecting upto");
    }
    else if (match(TokenType.DOWNTO)) {
      eat(TokenType.DOWNTO, "Expecting downto");
    }
    else {
      error("Syntax Error");
    }
    expr();
    eat(TokenType.LBRACE, "Expecting '{'");
    stmts();
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void ret_stmt() throws MyPLException {
    eat(TokenType.RETURN, "Expecting return");
    if (isExpr()) {
      expr();
    }
  }

  private void delete_stmt() throws MyPLException {
    eat(TokenType.DELETE, "Expecting delete");
    eat(TokenType.ID, "Expecting id");
  } 
}
