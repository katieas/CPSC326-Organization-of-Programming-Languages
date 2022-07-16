/*
 * File: CodeGenerator.java
 * Date: Spring 2022
 * Auth: Katie Stevens
 * Desc: 
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


public class CodeGenerator implements Visitor {

  // the user-defined type and function type information
  private TypeInfo typeInfo = null;

  // the virtual machine to add the code to
  private VM vm = null;

  // the current frame
  private VMFrame currFrame = null;

  // mapping from variables to their indices (in the frame)
  private Map<String,Integer> varMap = null;

  // the current variable index (in the frame)
  private int currVarIndex = 0;

  // to keep track of the typedecl objects for initialization
  Map<String,TypeDecl> typeDecls = new HashMap<>();


  //----------------------------------------------------------------------
  // HELPER FUNCTIONS
  //----------------------------------------------------------------------
  
  // helper function to clean up uneeded NOP instructions
  private void fixNoOp() {
    int nextIndex = currFrame.instructions.size();
    // check if there are any instructions
    if (nextIndex == 0)
      return;
    // get the last instuction added
    VMInstr instr = currFrame.instructions.get(nextIndex - 1);
    // check if it is a NOP
    if (instr.opcode() == OpCode.NOP)
      currFrame.instructions.remove(nextIndex - 1);
  }

  private void fixCallStmt(Stmt s) {
    // get the last instuction added
    if (s instanceof CallExpr) {
      VMInstr instr = VMInstr.POP();
      instr.addComment("clean up call return value");
      currFrame.instructions.add(instr);
    }

  }
  
  //----------------------------------------------------------------------  
  // Constructor
  //----------------------------------------------------------------------

  public CodeGenerator(TypeInfo typeInfo, VM vm) {
    this.typeInfo = typeInfo;
    this.vm = vm;
  }

  
  //----------------------------------------------------------------------
  // VISITOR FUNCTIONS
  //----------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {

    // store UDTs for later
    for (TypeDecl tdecl : node.tdecls) {
      // add a mapping from type name to the TypeDecl
      typeDecls.put(tdecl.typeName.lexeme(), tdecl);
    }
    // only need to translate the function declarations
    for (FunDecl fdecl : node.fdecls)
      fdecl.accept(this);
  }

  public void visit(TypeDecl node) throws MyPLException {
    // Intentionally left blank -- nothing to do here
  }
  
  public void visit(FunDecl node) throws MyPLException {
    
    currFrame = new VMFrame(node.funName.lexeme(), node.params.size());
    vm.add(currFrame);
    varMap = new HashMap<>();
    currVarIndex = 0;

    for (Integer i = 0; i < node.params.size(); ++i) {
      currFrame.instructions.add(VMInstr.STORE(i));
      varMap.put(node.params.get(i).paramName.lexeme(), i);
      currVarIndex++;
    }

    for (Stmt s : node.stmts) {
      s.accept(this);
      fixCallStmt(s);
    }

    // check to see if last statement was a return
    int stmtsSize = node.stmts.size();
    if ((stmtsSize == 0) || !(node.stmts.get(stmtsSize - 1) instanceof ReturnStmt)) {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
      currFrame.instructions.add(VMInstr.VRET());
    }
  }
  
  public void visit(VarDeclStmt node) throws MyPLException {
    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.STORE(currVarIndex));
    varMap.put(node.varName.lexeme(), Integer.valueOf(currVarIndex));
    currVarIndex++;
  }
  
  public void visit(AssignStmt node) throws MyPLException {
    node.expr.accept(this);
    if (node.lvalue.size() == 1) {
      int varIndex = varMap.get(node.lvalue.get(0).lexeme());
      currFrame.instructions.add(VMInstr.STORE(varIndex));
    }
    // for more complicated paths
    else {
      int varIndex = varMap.get(node.lvalue.get(0).lexeme());
      currFrame.instructions.add(VMInstr.LOAD(varIndex));
      for (int i = 1; i < node.lvalue.size(); ++i) {
        if (i == node.lvalue.size() - 1) { // last path
          currFrame.instructions.add(VMInstr.SWAP());
          currFrame.instructions.add(VMInstr.SETFLD(node.lvalue.get(i).lexeme()));
        }
        else {
          currFrame.instructions.add(VMInstr.GETFLD(node.lvalue.get(i).lexeme()));
        }  
      }
    }
  }
  
  public void visit(CondStmt node) throws MyPLException {
    ArrayList<Integer> jumps = new ArrayList<>();
    // if
    node.ifPart.cond.accept(this);
    int jumpF = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));
    for (Stmt s : node.ifPart.stmts)
      s.accept(this);
    jumps.add(currFrame.instructions.size());
    currFrame.instructions.add(VMInstr.JMP(-1)); // jump out of if statement
    currFrame.instructions.set(jumpF, VMInstr.JMPF(currFrame.instructions.size()));

    // elif
    for (BasicIf elif : node.elifs) {
      elif.cond.accept(this);
      jumpF = currFrame.instructions.size();
      currFrame.instructions.add(VMInstr.JMPF(-1));
      for (Stmt s : elif.stmts)
        s.accept(this);
      jumps.add(currFrame.instructions.size());
      currFrame.instructions.add(VMInstr.JMP(-1)); // jump out of if statement
      currFrame.instructions.set(jumpF, VMInstr.JMPF(currFrame.instructions.size()));
    }

    // else
    if (node.elseStmts != null) {
      for (Stmt s : node.elseStmts) {
        s.accept(this);
      }
    }

    for (int i = 0; i < jumps.size(); ++i) {
      currFrame.instructions.set(jumps.get(i), VMInstr.JMP(currFrame.instructions.size()));
    }
  }

  public void visit(WhileStmt node) throws MyPLException {
    int startAddress = currFrame.instructions.size();
    node.cond.accept(this);

    int jump = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));

    for (Stmt s : node.stmts)
      s.accept(this);

    currFrame.instructions.add(VMInstr.JMP(startAddress));
    currFrame.instructions.add(VMInstr.NOP());
    // update jmpf 
    currFrame.instructions.set(jump, VMInstr.JMPF(currFrame.instructions.size() - 1));
  }

  public void visit(ForStmt node) throws MyPLException {
    // store var
    int forVarIndex = currVarIndex;
    node.start.accept(this);
    currFrame.instructions.add(VMInstr.STORE(currVarIndex));
    varMap.put(node.varName.lexeme(), currVarIndex);
    currVarIndex++;
    
    int startAddress = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.LOAD(forVarIndex));
    node.end.accept(this);
    if (node.upto) {
      currFrame.instructions.add(VMInstr.CMPLE());
    }
    else {
      currFrame.instructions.add(VMInstr.CMPGE());
    }

    int jump = currFrame.instructions.size();
    currFrame.instructions.add(VMInstr.JMPF(-1));

    for (Stmt s : node.stmts)
      s.accept(this);
    
    // increment/decrement var
    if (node.upto) {
      currFrame.instructions.add(VMInstr.LOAD(forVarIndex));
      currFrame.instructions.add(VMInstr.PUSH(1));
      currFrame.instructions.add(VMInstr.ADD());
      currFrame.instructions.add(VMInstr.STORE(forVarIndex));
    }
    else {
      currFrame.instructions.add(VMInstr.LOAD(forVarIndex));
      currFrame.instructions.add(VMInstr.PUSH(1));
      currFrame.instructions.add(VMInstr.SUB());
      currFrame.instructions.add(VMInstr.STORE(forVarIndex));
    }
    

    currFrame.instructions.add(VMInstr.JMP(startAddress));
    currFrame.instructions.add(VMInstr.NOP());
    // update jmpf 
    currFrame.instructions.set(jump, VMInstr.JMPF(currFrame.instructions.size() - 1));
  }
  
  public void visit(ReturnStmt node) throws MyPLException {
    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.VRET());
  }
  
  
  public void visit(DeleteStmt node) throws MyPLException {
    int varIndex = varMap.get(node.varName.lexeme());
    currFrame.instructions.add(VMInstr.LOAD(varIndex));
    currFrame.instructions.add(VMInstr.FREE());
  }

  public void visit(CallExpr node) throws MyPLException {
    // push args (in order)
    for (Expr arg : node.args)
      arg.accept(this);
    // built-in functions:
    if (node.funName.lexeme().equals("print")) {
      currFrame.instructions.add(VMInstr.WRITE());
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    }
    else if (node.funName.lexeme().equals("read"))
      currFrame.instructions.add(VMInstr.READ());
    else if (node.funName.lexeme().equals("length")) 
      currFrame.instructions.add(VMInstr.LEN());
    else if (node.funName.lexeme().equals("get")) 
      currFrame.instructions.add(VMInstr.GETCHR());
    else if (node.funName.lexeme().equals("stoi") || node.funName.lexeme().equals("dtoi")) 
      currFrame.instructions.add(VMInstr.TOINT());
    else if (node.funName.lexeme().equals("stod") || node.funName.lexeme().equals("itod")) 
      currFrame.instructions.add(VMInstr.TODBL());
    else if (node.funName.lexeme().equals("itos") || node.funName.lexeme().equals("dtos")) 
      currFrame.instructions.add(VMInstr.TOSTR());
    // user-defined functions
    else
      currFrame.instructions.add(VMInstr.CALL(node.funName.lexeme()));
  }
  
  public void visit(SimpleRValue node) throws MyPLException {
    if (node.value.type() == TokenType.INT_VAL) {
      int val = Integer.parseInt(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
    }
    else if (node.value.type() == TokenType.DOUBLE_VAL) {
      double val = Double.parseDouble(node.value.lexeme());
      currFrame.instructions.add(VMInstr.PUSH(val));
    }
    else if (node.value.type() == TokenType.BOOL_VAL) {
      if (node.value.lexeme().equals("true"))
        currFrame.instructions.add(VMInstr.PUSH(true));
      else
        currFrame.instructions.add(VMInstr.PUSH(false));        
    }
    else if (node.value.type() == TokenType.CHAR_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
    }
    else if (node.value.type() == TokenType.STRING_VAL) {
      String s = node.value.lexeme();
      s = s.replace("\\n", "\n");
      s = s.replace("\\t", "\t");
      s = s.replace("\\r", "\r");
      s = s.replace("\\\\", "\\");
      currFrame.instructions.add(VMInstr.PUSH(s));
    }
    else if (node.value.type() == TokenType.NIL) {
      currFrame.instructions.add(VMInstr.PUSH(VM.NIL_OBJ));
    }
  }
  
  public void visit(NewRValue node) throws MyPLException {
    List<String> components = new ArrayList<>();
    TypeDecl tdecl = typeDecls.get(node.typeName.lexeme());
    for (VarDeclStmt vdecl : tdecl.vdecls)
      components.add(vdecl.varName.lexeme());
    currFrame.instructions.add(VMInstr.ALLOC(components));

    for (VarDeclStmt vdecl : tdecl.vdecls) {
      currFrame.instructions.add(VMInstr.DUP());
      vdecl.expr.accept(this);
      currFrame.instructions.add(VMInstr.SETFLD(vdecl.varName.lexeme()));
    }
  }
  
  public void visit(IDRValue node) throws MyPLException {
    int varIndex = varMap.get(node.path.get(0).lexeme());
    currFrame.instructions.add(VMInstr.LOAD(varIndex));

    // for more complex paths
    if (node.path.size() > 1) {
      for (int i = 1; i < node.path.size(); ++i) {
        currFrame.instructions.add(VMInstr.GETFLD(node.path.get(i).lexeme()));
      }
    }
  }
      
  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    currFrame.instructions.add(VMInstr.NEG());
  }

  public void visit(Expr node) throws MyPLException {
    node.first.accept(this);
    
    if (node.op != null) {
      node.rest.accept(this);
      if (node.op.type() == TokenType.PLUS)
        currFrame.instructions.add(VMInstr.ADD());
      else if (node.op.type() == TokenType.MINUS)
        currFrame.instructions.add(VMInstr.SUB());
      else if (node.op.type() == TokenType.MULTIPLY)
        currFrame.instructions.add(VMInstr.MUL());
      else if (node.op.type() == TokenType.DIVIDE)
        currFrame.instructions.add(VMInstr.DIV()); 
      else if (node.op.type() == TokenType.MODULO)
        currFrame.instructions.add(VMInstr.MOD());    
      else if (node.op.type() == TokenType.AND)
        currFrame.instructions.add(VMInstr.AND()); 
      else if (node.op.type() == TokenType.OR)
        currFrame.instructions.add(VMInstr.OR()); 
      else if (node.op.type() == TokenType.LESS_THAN)
        currFrame.instructions.add(VMInstr.CMPLT());  
      else if (node.op.type() == TokenType.LESS_THAN_EQUAL)
        currFrame.instructions.add(VMInstr.CMPLE()); 
      else if (node.op.type() == TokenType.GREATER_THAN)
        currFrame.instructions.add(VMInstr.CMPGT()); 
      else if (node.op.type() == TokenType.GREATER_THAN_EQUAL)
        currFrame.instructions.add(VMInstr.CMPGE()); 
      else if (node.op.type() == TokenType.EQUAL)
      currFrame.instructions.add(VMInstr.CMPEQ()); 
      else if (node.op.type() == TokenType.NOT_EQUAL)
        currFrame.instructions.add(VMInstr.CMPNE()); 
    }

    if (node.logicallyNegated) {
      currFrame.instructions.add(VMInstr.NOT());
    }
  }

  public void visit(SimpleTerm node) throws MyPLException {
    // defer to contained rvalue
    node.rvalue.accept(this);
  }
  
  public void visit(ComplexTerm node) throws MyPLException {
    // defer to contained expression
    node.expr.accept(this);
  }

}
