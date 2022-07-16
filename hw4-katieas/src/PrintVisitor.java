/*
 * File: PrintVisitor.java
 * Date: Spring 2022
 * Auth: 
 * Desc: 
 */

import java.io.PrintStream;


public class PrintVisitor implements Visitor {

  // output stream for printing
  private PrintStream out;
  // current indent level (number of spaces)
  private int indent = 0;
  // indentation amount
  private final int INDENT_AMT = 2;
  
  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private String getIndent() {
    return " ".repeat(indent);
  }

  private void incIndent() {
    indent += INDENT_AMT;
  }

  private void decIndent() {
    indent -= INDENT_AMT;
  }

  //------------------------------------------------------------
  // VISITOR FUNCTIONS
  //------------------------------------------------------------

  // Hint: To help deal with call expressions, which can be statements
  // or expressions, statements should not indent themselves and add
  // newlines. Instead, the function asking statements to print
  // themselves should add the indent and newlines.
  

  // constructor
  public PrintVisitor(PrintStream printStream) {
    out = printStream;
  }

  
  // top-level nodes

  @Override
  public void visit(Program node) throws MyPLException {
    // print type decls first
    for (TypeDecl d : node.tdecls)
      d.accept(this);
    // print function decls second
    for (FunDecl d : node.fdecls)
      d.accept(this);
  }


  // TODO: Finish the rest of the visitor functions ...

  @Override
  public void visit(TypeDecl node) throws MyPLException {
    System.out.println("type " + node.typeName.lexeme() + " {");
    incIndent();
    for (VarDeclStmt d : node.vdecls) {
      System.out.print(getIndent());
      d.accept(this);
      System.out.println();
    }
    decIndent();
    System.out.println("}\n");
  }

  @Override
  public void visit(FunDecl node) throws MyPLException {
    System.out.print("fun " + node.returnType.lexeme() + " " + node.funName.lexeme() + "(");
    for (int i = 0; i < node.params.size(); i++) { // param args
      System.out.print(node.params.get(i).paramType.lexeme() + " " + node.params.get(i).paramName.lexeme());
      if (i != node.params.size() - 1) {
        System.out.print(", ");
      }
    }
    System.out.println(") {");
    incIndent();
    for (Stmt d : node.stmts) {
      System.out.print(getIndent());
      d.accept(this);
      System.out.println();
    }
    decIndent();
    System.out.println("}\n");
  }

  @Override
  public void visit(Expr node) throws MyPLException {
    if (node.logicallyNegated) {
      System.out.print("(not ");
    }
  
    if (node.op != null) {
      System.out.print("(");
      node.first.accept(this);
      System.out.print(" " + node.op.lexeme() + " ");
      node.rest.accept(this);
      System.out.print(")");
    }
    else {
      node.first.accept(this);
    }

    if (node.logicallyNegated) {
      System.out.print(")");
    }
  }

  @Override
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  @Override
  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }
  
  @Override
  public void visit(SimpleRValue node) throws MyPLException {
    if (node.value.type() == TokenType.STRING_VAL) {
      System.out.print("\"" + node.value.lexeme() + "\"");
    }
    else {
      System.out.print(node.value.lexeme());
    }
  }

  @Override
  public void visit(NewRValue node) throws MyPLException {
    System.out.print("new " + node.typeName.lexeme());
  }

  @Override
  public void visit(IDRValue node) throws MyPLException {
    for (int i = 0; i < node.path.size(); i++) {
      System.out.print(node.path.get(i).lexeme());
      if (i != node.path.size() - 1) {
        System.out.print(".");
      }
    }
  }

  @Override
  public void visit(NegatedRValue node) throws MyPLException {
    System.out.print("neg ");
    node.expr.accept(this);
  }

  @Override
  public void visit(VarDeclStmt node) throws MyPLException {
    System.out.print("var ");
    if (node.typeName != null) {
      System.out.print(node.typeName.lexeme() + " ");
    }
    System.out.print(node.varName.lexeme() + " = ");
    node.expr.accept(this);
  }

  @Override
  public void visit(AssignStmt node) throws MyPLException {
    //System.out.print(getIndent());
    for (int i = 0; i < node.lvalue.size(); i++) {
      System.out.print(node.lvalue.get(i).lexeme());
      if (i != node.lvalue.size() - 1) {
        System.out.print(".");
      }
    }
    System.out.print(" = ");
    node.expr.accept(this);
  }

  @Override
  public void visit(ReturnStmt node) throws MyPLException {
    System.out.print("return ");
    node.expr.accept(this);
  }

  @Override
  public void visit(DeleteStmt node) throws MyPLException {
    System.out.println("delete " + node.varName.lexeme());
  }

  @Override
  public void visit(WhileStmt node) throws MyPLException {
    System.out.print("while ");
    node.cond.accept(this);
    System.out.println(" {");
    incIndent();
    for (Stmt d : node.stmts) {
      System.out.print(getIndent());
      d.accept(this);
      System.out.println();
    }
    decIndent();
    System.out.print(getIndent() + "}");
  }

  @Override
  public void visit(ForStmt node) throws MyPLException {
    System.out.print("for " + node.varName.lexeme() + " from ");
    node.start.accept(this);
    if (node.upto) {
      System.out.print(" upto ");
    }
    else {
      System.out.print(" downto ");
    }
    node.end.accept(this);
    System.out.println(" {");
    incIndent();
    for (Stmt d : node.stmts) {
      System.out.print(getIndent());
      d.accept(this);
      System.out.println();
    }
    decIndent();
    System.out.print(getIndent() + "}");

  }

  @Override
  public void visit(CondStmt node) throws MyPLException {
    System.out.print("if ");
    node.ifPart.cond.accept(this);
    System.out.println(" {");
    incIndent();
    for (Stmt d : node.ifPart.stmts) {
      System.out.print(getIndent());
      d.accept(this);
      System.out.println();
    }
    decIndent();
    System.out.print(getIndent() + "}");
    if (node.elifs.isEmpty() && node.elseStmts != null) {
      System.out.println();
    }
    if (!node.elifs.isEmpty()) { 
      System.out.println();
      for (BasicIf d : node.elifs) {
        System.out.print(getIndent() + "elif ");
        d.cond.accept(this);
        System.out.println(" {");
        incIndent();
        for (Stmt s : d.stmts) {
          System.out.print(getIndent());
          s.accept(this);
          System.out.println();
        }
        decIndent();
        System.out.println(getIndent() + "}");
      }
    }
    if (node.elseStmts != null) {
      System.out.println(getIndent() + "else {");
      incIndent();
      for (Stmt s : node.elseStmts) {
        System.out.print(getIndent());
        s.accept(this);
        System.out.println();
      }
      decIndent();
      System.out.print(getIndent() + "}");
    }
  }

  @Override
  public void visit(CallExpr node) throws MyPLException {
    System.out.print(node.funName.lexeme() + "(");
    for (int i = 0; i < node.args.size(); i ++) {
      node.args.get(i).accept(this);
      if (i != node.args.size() - 1) {
        System.out.print(", ");
      }
    }
    System.out.print(")");
  }


}
