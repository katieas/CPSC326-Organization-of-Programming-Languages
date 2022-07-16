/* 
 * File: ASTParser.java
 * Date: Spring 2022
 * Auth: 
 * Desc:
 */

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.InvalidRelationServiceException;


public class ASTParser {

  private Lexer lexer = null; 
  private Token currToken = null;
  private final boolean DEBUG = false;

  /** 
   */
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   */
  public Program parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    Program progNode = new Program();
    advance();
    while (!match(TokenType.EOS)) {
      if (match(TokenType.TYPE))
        tdecl(progNode);
      else
        fdecl(progNode);
    }
    advance(); // eat the EOS token
    return progNode;
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

  private void fKey(FunDecl fun) {
    String s = "";
    s = s + fun.funName.lexeme();

    for (FunParam p : fun.params) {
      s = s + "_" + p.paramType.lexeme();
    }
    fun.funParamKey = s;
  }

  // private void fKey(CallExpr callExpr) {
  //   String s = "";
  //   s = s + callExpr.funName.lexeme();

  //   for (FunParam p : fun.params) {
  //     s = s + "_" + p.paramType.lexeme();
  //   }
  //   fun.funParamKey = s;
  // }


  //------------------------------------------------------------
  // Recursive Descent Functions 
  //------------------------------------------------------------


  // TODO: Add your recursive descent functions from HW-3
  // and extend them to build up the AST

  private void tdecl(Program progNode) throws MyPLException {
    TypeDecl type = new TypeDecl();
    eat(TokenType.TYPE, "Expecting type");
    type.typeName = currToken;
    eat(TokenType.ID, "Expecting id");
    eat(TokenType.LBRACE, "Expecting '{");
    // <vdecls>
    while (match(TokenType.VAR)) {
      VarDeclStmt s = new VarDeclStmt();
      vdecl_stmt(s);
      type.vdecls.add(s);
    }
    eat(TokenType.RBRACE, "Expecting '}'");
    progNode.tdecls.add(type);
  }

  private void fdecl(Program progNode) throws MyPLException {
    FunDecl fun = new FunDecl();
    eat(TokenType.FUN, "Expecting fun");
    // <dtype> | VOID
    fun.returnType = currToken;
    if (match(TokenType.VOID_TYPE)) {
      eat(TokenType.VOID_TYPE, "Expecting void");
    } else {
      dtype();
    }
    fun.funName = currToken;
    eat(TokenType.ID, "Expecting ID");
    eat(TokenType.LPAREN, "Expecting '('");
    List<FunParam> p = new ArrayList<>();
    params(p);
    fun.params = p;
    eat(TokenType.RPAREN, "Expecting ')'"); 
    fKey(fun); // add the function key  
    eat(TokenType.LBRACE, "Expecting '{");
    List<Stmt> s = new ArrayList<>();
    stmts(s);
    fun.stmts = s;
    eat(TokenType.RBRACE, "Expecting '}'");
    progNode.fdecls.add(fun);
  }

  private void vdecl_stmt(VarDeclStmt s) throws MyPLException {
    eat(TokenType.VAR, "Expecting var");
    if (isPrimitiveType() || match(TokenType.ID)) {
      s.typeName = currToken;
      dtype();
    }
    
    if (match(TokenType.ID)) {
      s.varName = currToken;
      eat(TokenType.ID, "Expecting id");
    }
    else {
      s.varName = s.typeName;
      s.typeName = null;
    }
    eat(TokenType.ASSIGN, "Expecting '='");
    Expr e = new Expr();
    expr(e);
    s.expr = e;
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

  private void params(List<FunParam> pList) throws MyPLException {
    if (!match(TokenType.RPAREN)) {
      FunParam p = new FunParam();
      p.paramType = currToken;
      dtype();
      p.paramName = currToken;
      eat(TokenType.ID, "Expecting id");
      pList.add(p);
      while (match(TokenType.COMMA)) {
        p = new FunParam();
        eat(TokenType.COMMA, "Expecting ','");
        p.paramType = currToken;
        dtype();
        p.paramName = currToken;
        eat(TokenType.ID, "Expecting id");
        pList.add(p);
      }
    }
  }

  private void expr(Expr e) throws MyPLException {
    if (match(TokenType.NOT)) {
      e.logicallyNegated = true;
      eat(TokenType.NOT, "Expecting not");
      ComplexTerm c = new ComplexTerm();
      Expr newExpr = new Expr();
      expr(newExpr);
      c.expr = newExpr;
      e.first = c;
    }
    else if (match(TokenType.LPAREN)) {
      eat(TokenType.LPAREN, "Expecting '('");
      ComplexTerm c = new ComplexTerm();
      Expr newExpr = new Expr();
      expr(newExpr);
      c.expr = newExpr;
      e.first = c;
      eat(TokenType.RPAREN, "Expecting ')'");
    }
    else { // <rvalue>
      SimpleTerm t = new SimpleTerm();
      rvalue(t);
      e.first = t;
    }

    if (isOperator()) {
      e.op = currToken;
      operator();
      Expr newExpr = new Expr();
      expr(newExpr);
      e.rest = newExpr;
    }
  }

  private void rvalue(SimpleTerm t) throws MyPLException {
    if (isPrimitiveValue()) {
      SimpleRValue simpleRVal = new SimpleRValue();
      simpleRVal.value = currToken;
      t.rvalue = simpleRVal;
      pval();
    }
    else if (match(TokenType.NIL)) {
      SimpleRValue simpleRVal = new SimpleRValue();
      simpleRVal.value = currToken;
      t.rvalue = simpleRVal;
      eat(TokenType.NIL, "Expecting nil");
    }
    else if (match(TokenType.NEW)) {
      NewRValue newR = new NewRValue();
      eat(TokenType.NEW, "Expecting new");
      newR.typeName = currToken;
      eat(TokenType.ID, "Expecting id");
      t.rvalue = newR;
    }
    else if (match(TokenType.NEG)) {
      NegatedRValue negR = new NegatedRValue();
      eat(TokenType.NEG, "Expecting neg");
      Expr e = new Expr();
      expr(e);
      negR.expr = e;
      t.rvalue = negR;
    }
    else if (match(TokenType.ID)) { // if ID
      Token tmpToken = currToken;
      eat(TokenType.ID, "Expecting id");
       if (match(TokenType.LPAREN)) {
        CallExpr callExpr = new CallExpr();
        callExpr.funName = tmpToken;
        call_expr(callExpr);
        t.rvalue = callExpr;
      }
      else {
        IDRValue idrVal = new IDRValue();
        List<Token> pathList = new ArrayList<>();
        pathList.add(tmpToken);
        idrval(pathList);
        idrVal.path = pathList;
        t.rvalue = idrVal;
      }
      
    }
    else {
      error("Expecting rvalue");
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
      error("Expecting primitive value");
    }
  }

  private void call_expr(CallExpr callExpr) throws MyPLException {
    eat(TokenType.LPAREN, "Expecting '('");
    List<Expr> exprList = new ArrayList<>();
    args(exprList);
    callExpr.args = exprList;
    eat(TokenType.RPAREN, "Expecting ')'");
  }

  private void idrval(List<Token> pathList) throws MyPLException {
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expecting '.'");
      pathList.add(currToken);
      eat(TokenType.ID, "Expecting id");
    }
  }

  private void args(List<Expr> exprList) throws MyPLException {
    if (isExpr()) {
      Expr e = new Expr();
      expr(e);
      exprList.add(e);
      while (match(TokenType.COMMA)) {
        eat(TokenType.COMMA, "Expecting ','");
        e = new Expr();
        expr(e);
        exprList.add(e);
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

  private void stmts(List<Stmt> s) throws MyPLException {
    while (match(TokenType.VAR) || match(TokenType.ID) || match(TokenType.IF) ||
           match(TokenType.WHILE) || match(TokenType.FOR) || match(TokenType.RETURN) ||
           match(TokenType.DELETE)) {
      stmt(s);
    }
  }

  private void stmt(List<Stmt> s) throws MyPLException {
    if (match(TokenType.VAR)) {
      VarDeclStmt varStmt = new VarDeclStmt();
      vdecl_stmt(varStmt);
      s.add(varStmt);
    }
    else if (match(TokenType.ID)) {
      Token tmpToken = currToken;
      eat(TokenType.ID, "Expecting id");
      if (match(TokenType.LPAREN)) {
        CallExpr callExpr = new CallExpr();
        callExpr.funName = tmpToken;
        call_expr(callExpr);
        s.add(callExpr);
      }
      else {
        AssignStmt assignStmt = new AssignStmt();
        assignStmt.lvalue.add(tmpToken);
        assign_stmt(assignStmt);
        s.add(assignStmt);
      }
    }
    else if (match(TokenType.IF)) {
      CondStmt condStmt = new CondStmt();
      cond_stmt(condStmt);
      s.add(condStmt);
    }
    else if (match(TokenType.WHILE)) {
      WhileStmt whileStmt = new WhileStmt();
      while_stmt(whileStmt);
      s.add(whileStmt);
    }
    else if (match(TokenType.FOR)) {
      ForStmt forStmt = new ForStmt();
      for_stmt(forStmt);
      s.add(forStmt);
    }
    else if (match(TokenType.RETURN)) {
      ReturnStmt returnStmt = new ReturnStmt();
      ret_stmt(returnStmt);
      s.add(returnStmt);
    }
    else if (match(TokenType.DELETE)) {
      DeleteStmt deleteStmt = new DeleteStmt();
      delete_stmt(deleteStmt);
      s.add(deleteStmt);
    }
  }

  private void assign_stmt(AssignStmt assignStmt) throws MyPLException {
    //eat(TokenType.ID, "Expecting id");
    while (match(TokenType.DOT)) {
      eat(TokenType.DOT, "Expecting '.'");
      assignStmt.lvalue.add(currToken);
      eat(TokenType.ID, "Expecting id");
    }
    eat(TokenType.ASSIGN, "Expecting '='");
    Expr e = new Expr();
    expr(e);
    assignStmt.expr = e;
  }

  private void cond_stmt(CondStmt condStmt) throws MyPLException {
    BasicIf basicIf = new BasicIf();
    condStmt.ifPart = basicIf;
    eat(TokenType.IF, "Expecting if");
    Expr e = new Expr();
    expr(e);
    basicIf.cond = e;
    eat(TokenType.LBRACE, "Expecting '{'");
    List<Stmt> s = new ArrayList<>();
    stmts(s);
    basicIf.stmts = s;
    eat(TokenType.RBRACE, "Expecting '}'");
    condt(condStmt);
  }

  private void condt(CondStmt condStmt) throws MyPLException {
    if (match(TokenType.ELIF)) {
      BasicIf basicIf = new BasicIf();
      eat(TokenType.ELIF, "Expecting elif");
      Expr e = new Expr();
      expr(e);
      basicIf.cond = e;
      eat(TokenType.LBRACE, "Expecting '{'");
      List<Stmt> s = new ArrayList<>();
      stmts(s);
      basicIf.stmts = s;
      eat(TokenType.RBRACE, "Expecting '}'");
      condStmt.elifs.add(basicIf);
      condt(condStmt);
    }
    else if (match(TokenType.ELSE)) {
      eat(TokenType.ELSE, "Expecting else");
      eat(TokenType.LBRACE, "Expecting '{'");
      List<Stmt> s = new ArrayList<>();
      stmts(s);
      eat(TokenType.RBRACE, "Expecting '}'");
      condStmt.elseStmts = s;
    }
  }

  private void while_stmt(WhileStmt whileStmt) throws MyPLException {
    eat(TokenType.WHILE, "Expecting while");
    Expr e = new Expr();
    expr(e);
    whileStmt.cond = e;
    eat(TokenType.LBRACE, "Expecting '{'");
    List<Stmt> s = new ArrayList<>();
    stmts(s);
    whileStmt.stmts = s;
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void for_stmt(ForStmt forStmt) throws MyPLException {
    eat(TokenType.FOR, "Expecting for");
    forStmt.varName = currToken;
    eat(TokenType.ID, "Expecting id");
    eat(TokenType.FROM, "Expecting from");
    Expr e = new Expr();
    expr(e);
    forStmt.start = e;
    if (match(TokenType.UPTO)) {
      forStmt.upto = true;
      eat(TokenType.UPTO, "Expecting upto");
    }
    else if (match(TokenType.DOWNTO)) {
      forStmt.upto = false;
      eat(TokenType.DOWNTO, "Expecting downto");
    }
    else {
      error("Syntax Error");
    }
    e = new Expr();
    expr(e);
    forStmt.end = e;
    eat(TokenType.LBRACE, "Expecting '{'");
    List<Stmt> s = new ArrayList<>();
    stmts(s);
    forStmt.stmts = s;
    eat(TokenType.RBRACE, "Expecting '}'");
  }

  private void ret_stmt(ReturnStmt returnStmt) throws MyPLException {
    eat(TokenType.RETURN, "Expecting return");
    if (isExpr()) {
      Expr e = new Expr();
      expr(e);
      returnStmt.expr = e;
    }
  }

  private void delete_stmt(DeleteStmt deleteStmt) throws MyPLException {
    eat(TokenType.DELETE, "Expecting delete");
    deleteStmt.varName = currToken;
    eat(TokenType.ID, "Expecting id");
  } 
  
}
