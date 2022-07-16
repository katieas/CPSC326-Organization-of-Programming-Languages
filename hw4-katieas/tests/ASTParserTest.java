/*
 * File: ASTParserTest.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Basic unit tests for the MyPL ast-based parser class.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ASTParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    ASTParser parser = buildParser("");
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }

  @Test
  public void oneTypeDeclInProgram() throws Exception {
    String s = buildString
      ("type Node {",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }
  
  @Test
  public void oneFunDeclInProgram() throws Exception {
    String s = buildString
      ("fun void main() {",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void multipleTypeAndFunDeclsInProgram() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void F1() {}",
       "type T2 {}",
       "fun void F2() {}",
       "fun void main() {}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(2, p.tdecls.size());
    assertEquals(3, p.fdecls.size());
  }

  // my tests

  @Test
  public void numVarDeclsInTypeDeclInProgram() throws Exception {
    String s = buildString
    ("type T1 {",
     "  var int x = 1",
     "  var int y = 2",
     "  var int z = 3",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(3, p.tdecls.get(0).vdecls.size());
  }

  @Test
  public void numVarStmtsInFunInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     "  var int x = 1",
     "  var int y = 2",
     "  var int z = 3",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.fdecls.size());
    assertEquals(3, p.fdecls.get(0).stmts.size());
  }

  @Test
  public void singleIfStmtsInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     " if true {",
     "   var x = true",
     " }",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.fdecls.size());
    assertEquals(1, p.fdecls.get(0).stmts.size()); // one stmt
    assertEquals(0, ((CondStmt)p.fdecls.get(0).stmts.get(0)).elifs.size());
    assertEquals(null, ((CondStmt)p.fdecls.get(0).stmts.get(0)).elseStmts);
  }

  @Test
  public void complexIfStmtsInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     "  var x = 1",
     "  var y = 2",
     "  if (x > y) {",
     "    var z = true",
     "    var a = 1",
     "  } ",
     "  elif (x < y) {",
     "    var z = false",
     "  }",
     "  else {",
     "    var z = 1",
     "  }",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.fdecls.size());
    assertEquals(3, p.fdecls.get(0).stmts.size());
    assertEquals(1, ((CondStmt)p.fdecls.get(0).stmts.get(2)).elifs.size());
    assertEquals(1, ((CondStmt)p.fdecls.get(0).stmts.get(2)).elseStmts.size());
  }

  @Test
  public void nestedIfStmtsInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     "  if (true) {",
     "    if (true) {",
     "      x = true",
     "      y = false",
     "    }",
     "  }",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, ((CondStmt)p.fdecls.get(0).stmts.get(0)).ifPart.stmts.size()); 
    assertEquals(2, ((CondStmt)((CondStmt)p.fdecls.get(0).stmts.get(0)).ifPart.stmts.get(0)).ifPart.stmts.size()); 
  }

  @Test
  public void numVarAssignInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     "  x.y.z = 10",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(3, ((AssignStmt)p.fdecls.get(0).stmts.get(0)).lvalue.size());
  }

  @Test
  public void whileForStmtInProgram() throws Exception {
    String s = buildString
    ("fun void main() {",
     "  while (x < 3) {",
     "    x = x + 1",
     "  }",
     "  for i from 1 upto 3 {",
     "    x = x + 1",
     "  }",
     "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(2, p.fdecls.get(0).stmts.size());
  }

}
