/*
 * File: StatiCheckerTest.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Various static analysis tests for HW-5
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;


public class StaticCheckerTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------

  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }
  
  private static StaticChecker buildChecker() throws Exception {
    TypeInfo types = new TypeInfo();
    StaticChecker checker = new StaticChecker(types);
    return checker;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // BASIC FUNCTION AND TYPE DEFINITION CASES
  //------------------------------------------------------------

//   @Test
//   public void simpleProgramCheck() throws Exception {
//     String s = buildString("fun void main() {}");
//     buildParser(s).parse().accept(buildChecker());
//   }

//   @Test
//   public void missingMain() throws Exception {
//     String s = buildString("");
//     try {
//       buildParser(s).parse().accept(buildChecker());
//       fail("error not detected");
//     } catch(MyPLException ex) {
//       assertTrue(ex.getMessage().startsWith("STATIC_ERROR:"));
//     }
//   }
}