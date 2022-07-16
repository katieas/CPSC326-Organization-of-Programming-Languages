/*
 * File: MyPL.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: MyPL application runner for HW-2.
 */

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class MyPL {

  public static void main(String[] args) {
    try {

      boolean lexerMode = false;
      int argCount = args.length;

      // check for too many command line args
      if (argCount > 2) {
        displayUsageInfo();
        System.exit(1);
      }
      
      // check if in lexer or print mode
      if (argCount > 0 && args[0].equals("--lex"))
        lexerMode = true;

      // to check modes
      boolean specialMode = lexerMode;

      // check if incorrect args 
      if (argCount == 2 && !specialMode) {
        displayUsageInfo();
        System.exit(1);
      }

      // grab input file
      InputStream input = System.in;
      if (argCount == 2)
        input = new FileInputStream(args[1]);
      else if (argCount == 1 && !specialMode)
        input = new FileInputStream(args[0]);
      
      // create the lexer
      Lexer lexer = new Lexer(input);

      // run in lexer mode
      if (lexerMode) {
        Token t = lexer.nextToken();
        while (t.type() != TokenType.EOS) {
          System.out.println(t);
          t = lexer.nextToken();
        }
      }
    }
    catch (MyPLException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    catch (FileNotFoundException e) {
      int i = args.length == 1 ? 0 : 1;
      System.err.println("ERROR: Unable to open file '" + args[i] + "'");
      System.exit(1);
    }
  }

  private static void displayUsageInfo() {
    System.out.println("Usage: ./mypl [flag] [script-file]");
    System.out.println("Options:");
    System.out.println("  --lex      Display token information.");
  }
  
}
