/*
 * File: CodeGeneratorTest.java
 * Date: Spring 2022
 * Auth: S. Bowres
 * Desc: Basic unit tests for the MyPL code generator class. Note that
 *       these tests use the VM to test the code generator.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;


public class CodeGeneratorTest {

  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream(); 

  @Before
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @After
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static VM buildVM(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    Program program = parser.parse();
    TypeInfo  typeInfo = new TypeInfo();
    program.accept(new StaticChecker(typeInfo));
    VM vm = new VM();
    CodeGenerator genVisitor = new CodeGenerator(typeInfo, vm);
    program.accept(genVisitor);
    return vm;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }


  //------------------------------------------------------------
  // Basics
  //------------------------------------------------------------

//   @Test
//   public void emptyProgram() throws Exception {
//     String s = buildString
//       ("fun void main() {",
//        "}");
//     VM vm = buildVM(s);
//     vm.run();
//     assertEquals("", output.toString());
//   }

//   @Test
//   public void simplePrint() throws Exception {
//     String s = buildString
//       ("fun void main() {",
//        "  print(\"Hello World!\")",
//        "}");
//     VM vm = buildVM(s);
//     vm.run();
//     assertEquals("Hello World!", output.toString());
//   } 
}