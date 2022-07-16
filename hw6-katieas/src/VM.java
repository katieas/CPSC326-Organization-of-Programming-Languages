/*
 * File: VM.java
 * Date: Spring 2022
 * Auth: 
 * Desc: A bare-bones MyPL Virtual Machine. The architecture is based
 *       loosely on the architecture of the Java Virtual Machine
 *       (JVM).  Minimal error checking is done except for runtime
 *       program errors, which include: out of bound indexes,
 *       dereferencing a nil reference, and invalid value conversion
 *       (to int and double).
 */


import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Scanner;


/*----------------------------------------------------------------------

  TODO: Your main job for HW-6 is to finish the VM implementation
        below by finishing the handling of each instruction.

        Note that PUSH, NOT, JMP, READ, FREE, and NOP (trivially) are
        completed already to help get you started. 

        Be sure to look through OpCode.java to get a basic idea of
        what each instruction should do as well as the unit tests for
        additional details regarding the instructions.

        Note that you only need to perform error checking if the
        result would lead to a MyPL runtime error (where all
        compile-time errors are assumed to be found already). This
        includes things like bad indexes (in GETCHR), dereferencing
        and/or using a NIL_OBJ (see the ensureNotNil() helper
        function), and converting from strings to ints and doubles. An
        error() function is provided to help generate a MyPLException
        for such cases.

----------------------------------------------------------------------*/ 


class VM {

  // set to true to print debugging information
  private boolean DEBUG = false;
  
  // the VM's heap (free store) accessible via object-id
  private Map<Integer,Map<String,Object>> heap = new HashMap<>();
  
  // next available object-id
  private int objectId = 1111;
  
  // the frames for the program (one frame per function)
  private Map<String,VMFrame> frames = new HashMap<>();

  // the VM call stack
  private Deque<VMFrame> frameStack = new ArrayDeque<>();

  
  /**
   * For representing "nil" as a value
   */
  public static String NIL_OBJ = new String("nil");
  

  /** 
   * Add a frame to the VM's list of known frames
   * @param frame the frame to add
   */
  public void add(VMFrame frame) {
    frames.put(frame.functionName(), frame);
  }

  /**
   * Turn on/off debugging, which prints out the state of the VM prior
   * to each instruction. 
   * @param debug set to true to turn on debugging (by default false)
   */
  public void setDebug(boolean debug) {
    DEBUG = debug;
  }

  /**
   * Run the virtual machine
   */
  public void run() throws MyPLException {

    // grab the main stack frame
    if (!frames.containsKey("main"))
      throw MyPLException.VMError("No 'main' function");
    VMFrame frame = frames.get("main").instantiate();
    frameStack.push(frame);
    
    // run loop (keep going until we run out of frames or
    // instructions) note that we assume each function returns a
    // value, and so the second check below should never occur (but is
    // useful for testing, etc).
    while (frame != null && frame.pc < frame.instructions.size()) {
      // get next instruction
      VMInstr instr = frame.instructions.get(frame.pc);
      // increment instruction pointer
      ++frame.pc;

      // For debugging: to turn on the following, call setDebug(true)
      // on the VM.
      if (DEBUG) {
        System.out.println();
        System.out.println("\t FRAME........: " + frame.functionName());
        System.out.println("\t PC...........: " + (frame.pc - 1));
        System.out.println("\t INSTRUCTION..: " + instr);
        System.out.println("\t OPERAND STACK: " + frame.operandStack);
        System.out.println("\t HEAP ........: " + heap);
      }

      
      //------------------------------------------------------------
      // Consts/Vars
      //------------------------------------------------------------

      if (instr.opcode() == OpCode.PUSH) {
        frame.operandStack.push(instr.operand());
      }

      else if (instr.opcode() == OpCode.POP) {
        frame.operandStack.pop();
      }

      else if (instr.opcode() == OpCode.LOAD) {
        if (!(instr.operand() instanceof Integer)) {
          error("LOAD operand is not a valid integer", frame);
        }
        Integer index = (Integer)instr.operand();
        Object value = frame.variables.get(index);
        frame.operandStack.push(value);
      }
        
      else if (instr.opcode() == OpCode.STORE) {
        if (!(instr.operand() instanceof Integer)) {
          error("STORE operand is not a valid integer", frame);
        }
        Integer index = (Integer)instr.operand();
        Object value = frame.operandStack.pop();
        frame.variables.add(index, value);
      }

      
      //------------------------------------------------------------
      // Ops
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.ADD) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y + x);
        }
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y + x);
        }
        else if (frame.operandStack.peek() instanceof String || frame.operandStack.peek() instanceof Character) {
          String x = (String)frame.operandStack.pop();
          String y = (String)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y + x);
        }
        else {
          error("Invalid type for ADD", frame);
        }
        
      }

      else if (instr.opcode() == OpCode.SUB) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y - x);
        }
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y - x);
        }
        else {
          error("Invalid type for SUB", frame);
        }
      }

      else if (instr.opcode() == OpCode.MUL) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y * x);
        }
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y * x);
        }
        else {
          error("Invalid type for MUL", frame);
        }
      }

      else if (instr.opcode() == OpCode.DIV) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y / x);
        }
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y / x);
        }
        else {
          error("Invalid type for DIV", frame);
        }
      }

      else if (instr.opcode() == OpCode.MOD) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y % x);
        }
        else {
          error("Invalid type for MOD", frame);
        }
      }

      else if (instr.opcode() == OpCode.AND) {
        if (frame.operandStack.peek() instanceof Boolean) {
          Boolean x = (Boolean)frame.operandStack.pop();
          Boolean y = (Boolean)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y && x);
        }
        else {
          error("Invalid type for AND", frame);
        }
      }

      else if (instr.opcode() == OpCode.OR) {
        if (frame.operandStack.peek() instanceof Boolean) {
          Boolean x = (Boolean)frame.operandStack.pop();
          Boolean y = (Boolean)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y || x);
        }
        else {
          error("Invalid type for OR", frame);
        }
      }

      else if (instr.opcode() == OpCode.NOT) {
        Object operand = frame.operandStack.pop();
        ensureNotNil(frame, operand);
        frame.operandStack.push(!(boolean)operand);
      }

      else if (instr.opcode() == OpCode.CMPLT) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y < x);
        } 
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y < x);
        }
        else if (frame.operandStack.peek() instanceof String || frame.operandStack.peek() instanceof Character) {
          String x = (String)frame.operandStack.pop();
          String y = (String)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          Boolean value = y.compareTo(x) < 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid types for CMPLT", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPLE) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y <= x);
        } 
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y <= x);
        }
        else if (frame.operandStack.peek() instanceof String || frame.operandStack.peek() instanceof Character) {
          String x = (String)frame.operandStack.pop();
          String y = (String)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          Boolean value = y.compareTo(x) <= 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid type for CMPLE", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPGT) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y > x);
        } 
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y > x);
        }
        else if (frame.operandStack.peek() instanceof String || frame.operandStack.peek() instanceof Character) {
          String x = (String)frame.operandStack.pop();
          String y = (String)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          Boolean value = y.compareTo(x) > 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid type for CMPGT", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPGE) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          Integer y = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y >= x);
        } 
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          Double y = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push(y >= x);
        }
        else if (frame.operandStack.peek() instanceof String || frame.operandStack.peek() instanceof Character) {
          String x = (String)frame.operandStack.pop();
          String y = (String)frame.operandStack.pop();
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          Boolean value = y.compareTo(x) >= 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid type for CMPGE", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPEQ) {
        Object x = frame.operandStack.pop();
        Object y = frame.operandStack.pop();

        if (x.equals(NIL_OBJ) && y.equals(NIL_OBJ)) {
          frame.operandStack.push(true);
        }
        else if (x.equals(NIL_OBJ) || y.equals(NIL_OBJ)) {
          frame.operandStack.push(false);
        }
        else if (x instanceof Integer) {
          frame.operandStack.push((int)y == (int)x);
        } 
        else if (x instanceof Double) {
          frame.operandStack.push((double)y == (double)x);
        }
        else if (x instanceof String || x instanceof Character) {
          String s1 = x.toString();
          String s2 = y.toString();
          Boolean value = s2.compareTo(s1) == 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid type for CMPEQ", frame);
        }
      }

      else if (instr.opcode() == OpCode.CMPNE) {
        Object x = frame.operandStack.pop();
        Object y = frame.operandStack.pop();

        if (x.equals(NIL_OBJ) && y.equals(NIL_OBJ)) {
          frame.operandStack.push(false);
        }
        else if (x.equals(NIL_OBJ) || y.equals(NIL_OBJ)) {
          frame.operandStack.push(true);
        }
        else if (x instanceof Integer) {
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push((int)y != (int)x);
        } 
        else if (x instanceof Double) {
          ensureNotNil(frame, x);
          ensureNotNil(frame, y);
          frame.operandStack.push((double)y != (double)x);
        }
        else if (x instanceof String || x instanceof Character) {
          String s1 = x.toString();
          String s2 = y.toString();
          Boolean value = s2.compareTo(s1) != 0;
          frame.operandStack.push(value);
        }
        else {
          error("Invalid type for CMPNE", frame);
        }
      }

      else if (instr.opcode() == OpCode.NEG) {
        if (frame.operandStack.peek() instanceof Integer) {
          Integer x = (Integer)frame.operandStack.pop();
          ensureNotNil(frame, x);
          frame.operandStack.push(-x);
        }
        else if (frame.operandStack.peek() instanceof Double) {
          Double x = (Double)frame.operandStack.pop();
          ensureNotNil(frame, x);
          frame.operandStack.push(-x);
        }
        else {
          error("Invalid type for NEG", frame);
        }
        
      }

      
      //------------------------------------------------------------
      // Jumps
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.JMP) {
        frame.pc = (int)instr.operand();
      }

      else if (instr.opcode() == OpCode.JMPF) {
        if (frame.operandStack.peek() instanceof Boolean) {
          Boolean x = (Boolean)frame.operandStack.pop();

          // if x is false, jump
          if (!x) {
            frame.pc = (int)instr.operand();
          }
        }
      }
        
      //------------------------------------------------------------
      // Functions
      //------------------------------------------------------------

      else if (instr.opcode() == OpCode.CALL) {
        String funName = (String)instr.operand();
        VMFrame newFun = frames.get(funName).instantiate();
        for (int i = 0; i < newFun.argCount(); ++i) {
          Object args = frame.operandStack.pop();
          newFun.operandStack.push(args);
        }
        frameStack.push(newFun);
        frame = newFun;
      }
        
      else if (instr.opcode() == OpCode.VRET) {
        Object returnVal = frame.operandStack.pop();
        frameStack.pop();
        frame = frameStack.peek();
        frame.operandStack.push(returnVal);
      }
        
      //------------------------------------------------------------
      // Built-ins
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.WRITE) {
        System.out.print(frame.operandStack.pop());
      }

      else if (instr.opcode() == OpCode.READ) {
        Scanner s = new Scanner(System.in);
        frame.operandStack.push(s.nextLine());
      }

      else if (instr.opcode() == OpCode.LEN) {
        String x = (String)frame.operandStack.pop();
        ensureNotNil(frame, x);
        frame.operandStack.push(x.length());
      }

      else if (instr.opcode() == OpCode.GETCHR) {
        String x = (String)frame.operandStack.pop();
        Integer y = (Integer)frame.operandStack.pop();
        ensureNotNil(frame, x);
        ensureNotNil(frame, y);
        if (y >= x.length() || y < 0) {
          error("range not in bounds", frame);
        }
        else {
          frame.operandStack.push(x.substring(y, y + 1));
        }
        
      }

      else if (instr.opcode() == OpCode.TOINT) {
        Object x = frame.operandStack.pop();
        ensureNotNil(frame, x);
        if (x instanceof Double) {
          frame.operandStack.push(Integer.valueOf(((Double)x).intValue()));
        }
        else if (x instanceof String) {
          if (x.toString().matches("[0-9]+")) {
            frame.operandStack.push(Integer.valueOf(x.toString()));
          }
          else {
            error("string contains non numerical characters", frame);
          }
        }
        else {
          error("Incompatible type", frame);
        }
      }

      else if (instr.opcode() == OpCode.TODBL) {
        Object x = frame.operandStack.pop();
        ensureNotNil(frame, x);
        if (x instanceof Integer) {
          frame.operandStack.push(Double.valueOf((int)x));
        }
        else if (x instanceof String) {
          if (x.toString().matches("([0-9]*)\\.([0-9]*)")) {
            frame.operandStack.push(x.toString());
          }
          else {
            error("string contains non numerical characters", frame);
          }
        }
        else {
          error("Incompatible type", frame);
        }
        
      }

      else if (instr.opcode() == OpCode.TOSTR) {
        Object x = frame.operandStack.pop();
        ensureNotNil(frame, x);
        frame.operandStack.push(x.toString());
      }

      //------------------------------------------------------------
      // Heap related
      //------------------------------------------------------------

      else if (instr.opcode() == OpCode.ALLOC) {      
        List<String> fields = new ArrayList<>();
        fields.addAll((List<String>)instr.operand());
        Map<String, Object> tmp = new HashMap<>();
        for (String field : fields) {
          tmp.put(field, null);
        }
        heap.put(objectId, tmp);
        frame.operandStack.push(objectId);
        ++this.objectId;
      }

      else if (instr.opcode() == OpCode.FREE) {
        // pop the oid to 
        Object oid = frame.operandStack.pop();
        ensureNotNil(frame, oid);
        // remove the object with oid from the heap
        heap.remove((int)oid);
      }

      else if (instr.opcode() == OpCode.SETFLD) {
        String fName = (String)instr.operand();
        Object top = frame.operandStack.pop();
        Integer oid = (Integer)frame.operandStack.pop();
        Map<String, Object> field = heap.get(oid);
        field.replace(fName, top);
      }

      else if (instr.opcode() == OpCode.GETFLD) {      
        String fName = (String)instr.operand();
        Integer oid = (Integer)frame.operandStack.pop();
        Map<String, Object> field = heap.get(oid);
        frame.operandStack.push(field.get(fName));
      }

      //------------------------------------------------------------
      // Special instructions
      //------------------------------------------------------------
        
      else if (instr.opcode() == OpCode.DUP) {
        Object x = frame.operandStack.pop();
        ensureNotNil(frame, x);
        frame.operandStack.push(x);
        frame.operandStack.push(x);
      }

      else if (instr.opcode() == OpCode.SWAP) {
        Object x = frame.operandStack.pop();
        Object y = frame.operandStack.pop();
        ensureNotNil(frame, x);
        ensureNotNil(frame, y);
        frame.operandStack.push(x);
        frame.operandStack.push(y);
      }

      else if (instr.opcode() == OpCode.NOP) {
        // do nothing
      }

    }
  }

  
  // to print the lists of instructions for each VM Frame
  @Override
  public String toString() {
    String s = "";
    for (Map.Entry<String,VMFrame> e : frames.entrySet()) {
      String funName = e.getKey();
      s += "Frame '" + funName + "'\n";
      List<VMInstr> instructions = e.getValue().instructions;      
      for (int i = 0; i < instructions.size(); ++i) {
        VMInstr instr = instructions.get(i);
        s += "  " + i + ": " + instr + "\n";
      }
      // s += "\n";
    }
    return s;
  }

  
  //----------------------------------------------------------------------
  // HELPER FUNCTIONS
  //----------------------------------------------------------------------

  // error
  private void error(String m, VMFrame f) throws MyPLException {
    int pc = f.pc - 1;
    VMInstr i = f.instructions.get(pc);
    String name = f.functionName();
    m += " (in " + name + " at " + pc + ": " + i + ")";
    throw MyPLException.VMError(m);
  }

  // error if given value is nil
  private void ensureNotNil(VMFrame f, Object v) throws MyPLException {
    if (v == NIL_OBJ)
      error("Nil reference", f);
  }
}
