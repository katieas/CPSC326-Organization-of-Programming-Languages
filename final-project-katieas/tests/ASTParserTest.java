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
  public void functionIntParametersName() throws Exception {
      String s = buildString
      ("fun void f(int x, int y) {",
       "  x = y",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals("f_int_int", p.fdecls.get(0).funParamKey);
  }

  @Test
  public void funDifferentParamTypes() throws Exception {
      String s = buildString
      ("fun void f(int x, int y) {",
       "  x = y",
       "}",
       "fun void f(double x, double x) {",
       "  x = y",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals("f_int_int", p.fdecls.get(0).funParamKey);
    assertEquals("f_double_double", p.fdecls.get(1).funParamKey);
  }

  @Test
  public void funDifferentParamSequence() throws Exception {
      String s = buildString
      ("fun void f(int x, double y) {",
       "  x = 1",
       "}",
       "fun void f(double x, int y) {",
       "  x = 1.0",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals("f_int_double", p.fdecls.get(0).funParamKey);
    assertEquals("f_double_int", p.fdecls.get(1).funParamKey);
  }

  @Test
  public void funDifferentParamNum() throws Exception {
      String s = buildString
      ("fun void f(String s1) {",
       "  s1 = \"Hello\"",
       "}",
       "fun void f(String s1, String s2) {",
       "  s1 = s2",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals("f_String", p.fdecls.get(0).funParamKey);
    assertEquals("f_String_String", p.fdecls.get(1).funParamKey);
  }
}
