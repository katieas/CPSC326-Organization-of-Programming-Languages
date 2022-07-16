/*
 * File: StaticChecker.java
 * Date: Spring 2022
 * Auth: Katie Stevens
 * Desc: HW5 static checker
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


// NOTE: Some of the following are filled in, some partly filled in,
// and most left for you to fill in. The helper functions are provided
// for you to use as needed. 


public class StaticChecker implements Visitor {

  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();
  // the current expression type
  private String currType = null;
  // the program's user-defined (record) types and function signatures
  private TypeInfo typeInfo = null;

  //--------------------------------------------------------------------
  // helper functions:
  //--------------------------------------------------------------------
  
  // generate an error
  private void error(String msg, Token token) throws MyPLException {
    String s = msg;
    if (token != null)
      s += " near line " + token.line() + ", column " + token.column();
    throw MyPLException.StaticError(s);
  }

  // return all valid types
  // assumes user-defined types already added to symbol table
  private List<String> getValidTypes() {
    List<String> types = new ArrayList<>();
    types.addAll(Arrays.asList("int", "double", "bool", "char", "string",
                               "void"));
    for (String type : typeInfo.types())
      if (symbolTable.get(type).equals("type"))
        types.add(type);
    return types;
  }

  // return the build in function names
  private List<String> getBuiltinFunctions() {
    return Arrays.asList("print", "read", "length", "get", "stoi",
                         "stod", "itos", "itod", "dtos", "dtoi");
  }
  
  // check if given token is a valid function signature return type
  private void checkReturnType(Token typeToken) throws MyPLException {
    if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  // helper to check if the given token is a valid parameter type
  private void checkParamType(Token typeToken) throws MyPLException {
    if (typeToken.equals("void"))
      error("'void' is an invalid parameter type", typeToken);
    else if (!getValidTypes().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  
  // helpers to get first token from an expression for calls to error
  
  private Token getFirstToken(Expr expr) {
    return getFirstToken(expr.first);
  }

  private Token getFirstToken(ExprTerm term) {
    if (term instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)term).rvalue);
    else
      return getFirstToken(((ComplexTerm)term).expr);
  }

  private Token getFirstToken(RValue rvalue) {
    if (rvalue instanceof SimpleRValue)
      return ((SimpleRValue)rvalue).value;
    else if (rvalue instanceof NewRValue)
      return ((NewRValue)rvalue).typeName;
    else if (rvalue instanceof IDRValue)
      return ((IDRValue)rvalue).path.get(0);
    else if (rvalue instanceof CallExpr)
      return ((CallExpr)rvalue).funName;
    else 
      return getFirstToken(((NegatedRValue)rvalue).expr);
  }

  
  //---------------------------------------------------------------------
  // constructor
  //--------------------------------------------------------------------
  
  public StaticChecker(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
  

  //--------------------------------------------------------------------
  // top-level nodes
  //--------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {
    // push the "global" environment
    symbolTable.pushEnvironment();

    // (1) add each user-defined type name to the symbol table and to
    // the list of rec types, check for duplicate names
    for (TypeDecl tdecl : node.tdecls) {
      String t = tdecl.typeName.lexeme();
      if (symbolTable.nameExists(t))
        error("type '" + t + "' already defined", tdecl.typeName);
      // add as a record type to the symbol table
      symbolTable.add(t, "type");
      // add initial type info (rest added by TypeDecl visit function)
      typeInfo.add(t);
    }
    
    // TODO: (2) add each function name and signature to the symbol
    // table check for duplicate names
    for (FunDecl fdecl : node.fdecls) {
      String funName = fdecl.funName.lexeme();
      // make sure not redefining built-in functions
      if (getBuiltinFunctions().contains(funName)) {
        String m = "cannot redefine built in function " + funName;
        error(m, fdecl.funName);
      }
      // check if function already exists
      if (symbolTable.nameExists(funName))
        error("function '" + funName + "' already defined", fdecl.funName);

      // TODO: Build the function param names and signature.
      // make sure the return type is a valid type
      checkReturnType(fdecl.returnType);
      // add to the symbol table as a function
      symbolTable.add(funName, "fun");
      // add to typeInfo
      typeInfo.add(funName);

      for (FunParam p : fdecl.params) {
        // check for duplicates
        List<String> currParams = new ArrayList<>(typeInfo.components(funName));
        for (int i = 0; i < currParams.size(); i++) {
          if (currParams.get(i).equals(p.paramName.lexeme())) {
            error("function cannot have duplicate parameters", fdecl.funName);
          }
        }
        typeInfo.add(funName, p.paramName.lexeme(), p.paramType.lexeme());
      }

      // TODO: add each formal parameter as a component type
      // add the return type
      typeInfo.add(funName, "return", fdecl.returnType.lexeme());
    }

    // TODO: (3) ensure "void main()" defined and it has correct
    // signature
    if (!symbolTable.nameExistsInCurrEnv("main")) 
      error("undefined 'main' function", new Token(null, currType, 0, 0));

    List<String> params = new ArrayList<>(typeInfo.components("main"));
    if (params.size() != 1) {
      error("main should have no paramters", new Token(null, currType, 0, 0));
    }

    String returnType = typeInfo.get("main", "return");
    if (!returnType.equals("void")) 
      error("main function must return type void", new Token(null, currType, 0, 0));
  
    
    // check each type and function
    for (TypeDecl tdecl : node.tdecls) 
      tdecl.accept(this);
    for (FunDecl fdecl : node.fdecls) 
      fdecl.accept(this);

    // all done, pop the global table
    symbolTable.popEnvironment();
  }
  

  public void visit(TypeDecl node) throws MyPLException {
    symbolTable.pushEnvironment();
    for (VarDeclStmt vdecl : node.vdecls) {
      vdecl.accept(this);
      typeInfo.add(node.typeName.lexeme(), vdecl.varName.lexeme(), currType);
    }
    symbolTable.popEnvironment();
  }

  
  public void visit(FunDecl node) throws MyPLException {
    currType = "void";
    //boolean hasReturn = false;
    symbolTable.pushEnvironment();
    String returnType = typeInfo.get(node.funName.lexeme(), "return");

    List<String> params = new ArrayList<>(typeInfo.components(node.funName.lexeme()));
    String name = "";
    String type = "";
    for (int i = 0; i < params.size(); ++i) {
      name = params.get(i);
      type = typeInfo.get(node.funName.lexeme(), name);
      symbolTable.add(name, type);
    }

    for (Stmt s : node.stmts) {
      s.accept(this);
      if (s instanceof ReturnStmt) {
        if (!returnType.equals(currType) && !currType.equals("void"))
          error("return types do not match", node.funName);
      }
    }
    symbolTable.popEnvironment();
  }


  //--------------------------------------------------------------------
  // statement nodes
  //--------------------------------------------------------------------
  
  public void visit(VarDeclStmt node) throws MyPLException {

    node.expr.accept(this);
    String varName = node.varName.lexeme();
    String exprType = currType;

    // variable shadowing
    if (symbolTable.nameExistsInCurrEnv(varName)) 
      error("variable already defined", node.varName); 

    // implicit def
    if (node.typeName == null) {
      if(exprType.equals("void")) 
        error("bad implicit variable declaration", node.varName);
    }
    else { // explicit def
      if (!node.typeName.lexeme().equals(exprType) && !exprType.equals("void")) 
        error("expected " + node.typeName.lexeme(), node.varName);
    }

    if(!exprType.equals("int") && !exprType.equals("double") && !exprType.equals("char") && !exprType.equals("bool") &&
       !exprType.equals("string") && !exprType.equals("void") && !(node.expr.first instanceof NewRValue)) {
      if(node.expr.first instanceof SimpleTerm) {
        SimpleTerm s = (SimpleTerm)node.expr.first;
        if(!(s.rvalue instanceof NewRValue)) {
          if(s.rvalue instanceof IDRValue) {
            IDRValue i = (IDRValue)s.rvalue;
            if(i.path.size() < 2) {
              error("expected a var, path, or new", getFirstToken(node.expr));
            }
          }
          else {
            error("expected a var, path, or new", getFirstToken(node.expr));
          }
        }
      } 
      else {
        error("expected a var", getFirstToken(node.expr));
      }
    }

    if (exprType.equals("void")) { 
      symbolTable.add(node.varName.lexeme(), node.typeName.lexeme());
      currType = node.typeName.lexeme();
    }
    else {
      symbolTable.add(node.varName.lexeme(), exprType);
    }
  }
  

  public void visit(AssignStmt node) throws MyPLException {
    node.expr.accept(this);
    String rhsType = currType;
    String varName = node.lvalue.get(0).lexeme();
    if (!symbolTable.nameExists(varName)) {
      error(varName + " is not defined", node.lvalue.get(0));
    }
    String lhsType = symbolTable.get(varName);

    // for more complex paths
    if (node.lvalue.size() > 1) {
      for (int i = 1; i < node.lvalue.size(); ++i) {
        if (!symbolTable.get(lhsType).equals("type")) {
          error(lhsType + " is not an object variable", node.lvalue.get(i - 1));
        }
        Set<String> components = typeInfo.components(lhsType);
        if (!components.contains(node.lvalue.get(i).lexeme())) {
          error("objects of type " + lhsType + " do not have field " + varName, node.lvalue.get(i - 1));
        }
        else {
          lhsType = typeInfo.get(lhsType, node.lvalue.get(i).lexeme());
        }
      }
    }
    
    // check rhsType and lhsType are compatible
    if (!rhsType.equals("void") && !lhsType.equals(rhsType)) {
      error("incompatble types", getFirstToken(node.expr));
    }

  }
  
  
  public void visit(CondStmt node) throws MyPLException {
    node.ifPart.cond.accept(this);
    if (!currType.equals("bool")) {
      error("expression must be a bool", getFirstToken(node.ifPart.cond));
    }

    // Basic If
    symbolTable.pushEnvironment();
    for (Stmt s : node.ifPart.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();

    // Elif Statements
    for (BasicIf elif : node.elifs) {
      elif.cond.accept(this);
      if (!currType.equals("bool")) {
        error("expression must be a bool", getFirstToken(node.ifPart.cond));
      }
      symbolTable.pushEnvironment();
      for (Stmt s : elif.stmts) {
        s.accept(this);
      }
      symbolTable.popEnvironment();
    }
    
    // Else statements
    symbolTable.pushEnvironment();
    if (node.elseStmts != null) {
      for (Stmt s : node.elseStmts) {
        s.accept(this);
      }
    }
    symbolTable.popEnvironment();
  }
  

  public void visit(WhileStmt node) throws MyPLException {
    node.cond.accept(this);
    if (!currType.equals("bool")) {
      error("expression must be a bool", getFirstToken(node.cond));
    }

    symbolTable.pushEnvironment();
    for (Stmt s : node.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();
  }
  

  public void visit(ForStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    node.start.accept(this);
    if (!currType.equals("int")) {
      error("start must be type int", node.varName);
    }
    node.end.accept(this);
    if (!currType.equals("int")) {
      error("end must be type int", node.varName);
    }

    symbolTable.add(node.varName.lexeme(), "int");

    for (Stmt s : node.stmts) {
      s.accept(this);
    }
    symbolTable.popEnvironment();
  }
  
  
  public void visit(ReturnStmt node) throws MyPLException {
    node.expr.accept(this);
  }
  
  public void visit(DeleteStmt node) throws MyPLException {

    String varName = node.varName.lexeme();
    if (!symbolTable.nameExistsInCurrEnv(varName)) 
      error(varName + " does not exist", node.varName);
    
    if (!symbolTable.get(varName).equals("type")) 
      error(varName + " is not a user-defined type", node.varName);
    
    Set<String> types = typeInfo.types();
    if (!types.contains(symbolTable.get(varName))) 
      error("is not a user-defined type", node.varName);
  }
  

  //----------------------------------------------------------------------
  // statement and rvalue node
  //----------------------------------------------------------------------

  private void checkBuiltIn(CallExpr node) throws MyPLException {
    String funName = node.funName.lexeme();
    if (funName.equals("print")) {
      // has to have one argument, any type is allowed
      if (node.args.size() != 1)
        error("print expects one argument", node.funName);
      currType = "void";
    }
    else if (funName.equals("read")) {
      // no arguments allowed
      if (node.args.size() != 0)
        error("read takes no arguments", node.funName);
      currType = "string";
    }
   else if (funName.equals("length")) {
      // one string argument
      if (node.args.size() != 1)
        error("length expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in length", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("get")) {
      // two arguments
      if (node.args.size() != 2) 
        error("get expects two arguments", node.funName);
      Expr e1 = node.args.get(0);
      e1.accept(this);
      if (!currType.equals("int"))
        error("expecting int in get", getFirstToken(e1));
      Expr e2 = node.args.get(1);
      e2.accept(this);
      if (!currType.equals("string"))
        error("expecting string in get", getFirstToken(e2));
      currType = "string";
    }
    else if (funName.equals("stoi")) {
      // one string argument
      if (node.args.size() != 1)
        error("stoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in stoi", getFirstToken(e));
      currType = "int";
    }
    else if (funName.equals("stod")) {
      // one string argument
      if (node.args.size() != 1)
        error("stod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string"))
        error("expecting string in stod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("itos")) {
      // one int argument
      if (node.args.size() != 1)
        error("itos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int"))
        error("expecting int in itos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("itod")) {
      // one int argument
      if (node.args.size() != 1)
        error("itod expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int"))
        error("expecting int in itod", getFirstToken(e));
      currType = "double";
    }
    else if (funName.equals("dtos")) {
      // one double argument
      if (node.args.size() != 1)
        error("dtos expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double"))
        error("expecting double in dtos", getFirstToken(e));
      currType = "string";
    }
    else if (funName.equals("dtoi")) {
      // one double argument
      if (node.args.size() != 1)
        error("dtoi expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double"))
        error("expecting double in dtoi", getFirstToken(e));
      currType = "int";
    }
  }

  
  public void visit(CallExpr node) throws MyPLException {
    String funName = node.funName.lexeme();
    // check built in functions
    if (getBuiltinFunctions().contains(funName)) {
      checkBuiltIn(node);
    }
    else {
      if (!symbolTable.nameExists(funName))
        error("fun " + funName + " does not exist", node.funName);

      List<String> components = new ArrayList<>(typeInfo.components(funName));
      String type = "";
      String name = "";
      if (node.args.size() != components.size() - 1) 
        error("incorrect number of args in function", node.funName);

      for (int i = 0; i < node.args.size(); ++i) {
        Expr e = node.args.get(i);
        e.accept(this);
        name = components.get(i);
        type = typeInfo.get(funName, name);
        if (!currType.equals(type) && !currType.equals("void")) 
          error("", getFirstToken(node.args.get(i)));
      }
      
      name = components.get(components.size() - 1);
      currType = typeInfo.get(funName, name);
    }
  }
  

  //----------------------------------------------------------------------
  // rvalue nodes
  //----------------------------------------------------------------------
  
  public void visit(SimpleRValue node) throws MyPLException {
    TokenType tokenType = node.value.type();
    if (tokenType == TokenType.INT_VAL)
      currType = "int";
    else if (tokenType == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (tokenType == TokenType.BOOL_VAL)
      currType = "bool";
    else if (tokenType == TokenType.CHAR_VAL)    
      currType = "char";
    else if (tokenType == TokenType.STRING_VAL)
      currType = "string";
    else if (tokenType == TokenType.NIL)
      currType = "void";
  }
  
    
  public void visit(NewRValue node) throws MyPLException {
    if (symbolTable.nameExists(node.typeName.lexeme())) {
      if (!symbolTable.get(node.typeName.lexeme()).equals("type")) {
        error(node.typeName.lexeme() + " is not an object variable", node.typeName);
      }
    }
    else { 
      error("type does not exist", node.typeName);
    }
    currType = node.typeName.lexeme();
  }
  
      
  public void visit(IDRValue node) throws MyPLException {
    
    if(!symbolTable.nameExists(node.path.get(0).lexeme())) {
      error(node.path.get(0).lexeme() + " does not exist", node.path.get(0));
    }

    String type = symbolTable.get(node.path.get(0).lexeme());
    if (node.path.size() > 1) {
      for (int i = 1; i < node.path.size(); ++i) {
        if (!symbolTable.get(type).equals("type")) {
          error(type + " is not an object variable", node.path.get(i - 1));
        }

        Set<String> components = typeInfo.components(type);
        if (!components.contains(node.path.get(i).lexeme())) {
          error("object of type " + type + " does not have field " + node.path.get(i).lexeme(), node.path.get(i - 1));
        }
        else {
          type = typeInfo.get(type, node.path.get(i).lexeme());
        }
      }
    }
    currType = type;
  }
  
      
  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    if (!(currType.equals("int") || currType.equals("double"))) {
      error("negated values can only be ints or doubles", getFirstToken(node.expr));
    }
  }
  

  //----------------------------------------------------------------------
  // expression node
  //----------------------------------------------------------------------
  
  public void visit(Expr node) throws MyPLException {
    node.first.accept(this);
    String lhsType = currType;
    if (node.op != null) {
      node.rest.accept(this);
      String rhsType = currType;
      String operator = node.op.lexeme();

      if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")) {
        if ((lhsType.equals("char") || lhsType.equals("string")) && (rhsType.equals("string") || rhsType.equals("char"))) {
          if (!operator.equals("+")) {
            error("incompatible types for " + operator, getFirstToken(node.first));
          }
          else if (lhsType.equals("char") && rhsType.equals(lhsType)) {
            error("cannot add two chars together", getFirstToken(node.first));
          }
          lhsType = "string";
        }
        else if (!(lhsType.equals("int") && rhsType.equals(lhsType)) && !(lhsType.equals("double") && rhsType.equals(lhsType))) {
          error(operator + " can only be used with two ints or two doubles", getFirstToken(node.first));
        }
        currType = lhsType;
      }
      else if (operator.equals("%")) {
        if (!(lhsType.equals("int") && rhsType.equals(lhsType))) {
          error("% can only be used with two ints", getFirstToken(node.first));
        }
      }
      else if (operator.equals("and") || operator.equals("or")) {
        if (!(lhsType.equals("bool") && rhsType.equals(lhsType))) {
          error("and/or can only be used with two bools", getFirstToken(node.first));
        }
        currType = "bool";
      }
      else if (operator.equals("==") || operator.equals("!=")) {
        if (!lhsType.equals(rhsType) && !(lhsType.equals("void") || rhsType.equals("void"))) {
          error(operator + " must be used with two of the same types", getFirstToken(node.first));
        }
        // else if (lhsType.equals(rhsType) || lhsType.equals("void")) {
        //   error(operator + " cannot be used to compare two void types", getFirstToken(node.first));
        // }
        currType = "bool";
      }
      else {
        // <, >, <=, >=
        if (!lhsType.equals(rhsType)) {
          error(operator + " cannot be used to compare different types", getFirstToken(node.first));
        }
        else if (!(lhsType.equals("int") || lhsType.equals("double") || lhsType.equals("char") || lhsType.equals("string"))) {
          error(operator + " can only be used with types int, double, char, and string", getFirstToken(node.first));
        }
        currType = "bool";
      }
    }

    

    if (node.logicallyNegated && currType != "bool") {
      error("can only logically negate bools", getFirstToken(node.first));
    }
  }


  //----------------------------------------------------------------------
  // terms
  //----------------------------------------------------------------------
  
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }
  

  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }
}