/*
 * File: HW6.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Example program to test the MyPL VM
 */


/*----------------------------------------------------------------------
   Your job for this part of the assignment is to imlement the
   following as a set of MyPL VM instructions and the VM. Note that
   you must implement the is_prime function and generally folow the
   approach laid out below. You can view the following as pseudocode
   (which could have been written in any procedural programming
   language). Note that since we don't have a square root function in
   MyPL, our naive primality tester is not very efficient.

    fun bool is_prime(int n) {
      var m = n / 2
      var v = 2
      while v <= m {
        var r = n / v
        var p = r * v
        if p == n {
          return false
        }
        v = v + 1
      }
      return true
    }

    fun void main() {
      print("Please enter integer values to sum (prime number to quit)\n")
      var sum = 0
      while true {
        print(">> Enter an int: ")
        var val = stoi(read())
        if is_prime(val) {
          print("The sum is: " + itos(sum) + "\n")
          print("Goodbye!\n")
          return
        }
        sum = sum + val
      }
    }
----------------------------------------------------------------------*/  

public class HW6 {

  public static void main(String[] args) throws Exception {
    VM vm = new VM();

    // is_prime
    VMFrame is_prime = new VMFrame("is_prime", 1);
    vm.add(is_prime);
    is_prime.instructions.add(VMInstr.STORE(0)); // n - line 0
    is_prime.instructions.add(VMInstr.LOAD(0)); // n - line 1
    is_prime.instructions.add(VMInstr.PUSH(2)); // line 2
    is_prime.instructions.add(VMInstr.DIV()); // n / 2 - line 3
    is_prime.instructions.add(VMInstr.STORE(1)); // m - line 4

    is_prime.instructions.add(VMInstr.PUSH(2)); // line 5
    is_prime.instructions.add(VMInstr.STORE(2)); // v - line 6

    // while
    is_prime.instructions.add(VMInstr.LOAD(2)); // v - line 7
    is_prime.instructions.add(VMInstr.LOAD(1)); // m - line 8
    is_prime.instructions.add(VMInstr.CMPLE()); // v <= m - line 9
    is_prime.instructions.add(VMInstr.JMPF(30));

    is_prime.instructions.add(VMInstr.LOAD(0)); // n - line 10
    is_prime.instructions.add(VMInstr.LOAD(2)); // v - line 11
    is_prime.instructions.add(VMInstr.DIV()); // n / v - line 12
    is_prime.instructions.add(VMInstr.STORE(3)); // r - line 13

    is_prime.instructions.add(VMInstr.LOAD(3)); // r - line 14
    is_prime.instructions.add(VMInstr.LOAD(2)); // v - line 15
    is_prime.instructions.add(VMInstr.MUL()); // r * v - line 16
    is_prime.instructions.add(VMInstr.STORE(4)); // p - line 17

    // if
    is_prime.instructions.add(VMInstr.LOAD(4)); // p - line 18
    is_prime.instructions.add(VMInstr.LOAD(0)); // n - line 19
    is_prime.instructions.add(VMInstr.CMPEQ()); // p == n - line 20
    is_prime.instructions.add(VMInstr.JMPF(25));

    is_prime.instructions.add(VMInstr.PUSH(false)); // return false - line 21
    is_prime.instructions.add(VMInstr.VRET()); // line 22

    is_prime.instructions.add(VMInstr.LOAD(2)); // v - line 23
    is_prime.instructions.add(VMInstr.PUSH(1)); // line 24
    is_prime.instructions.add(VMInstr.ADD()); // v + 1 - line 25
    is_prime.instructions.add(VMInstr.STORE(2)); // v - line 26

    is_prime.instructions.add(VMInstr.JMP(7)); // start of while - line 27
    is_prime.instructions.add(VMInstr.NOP()); // line 28

    is_prime.instructions.add(VMInstr.PUSH(true)); // line 29
    is_prime.instructions.add(VMInstr.VRET()); // line 30

    //main
    VMFrame main = new VMFrame("main", 0);
    vm.add(main);
    main.instructions.add(VMInstr.PUSH("Please enter integer values to sum (prime number to quit)\n")); // line 0
    main.instructions.add(VMInstr.WRITE()); // line 1
    main.instructions.add(VMInstr.PUSH(0)); // line 2
    main.instructions.add(VMInstr.STORE(0)); // sum - line 3

    // while true
    main.instructions.add(VMInstr.PUSH(">> Enter an int: ")); // line 4
    main.instructions.add(VMInstr.WRITE()); // line 5
    main.instructions.add(VMInstr.READ()); // read string - line 6
    main.instructions.add(VMInstr.TOINT()); // stoi - line 7
    main.instructions.add(VMInstr.STORE(1)); // val - line 8

    // if
    main.instructions.add(VMInstr.LOAD(1)); // val - line 9
    main.instructions.add(VMInstr.CALL("is_prime")); // is_prime(val) - line 10
    main.instructions.add(VMInstr.JMPF(22)); // line 11
    
    main.instructions.add(VMInstr.PUSH("The sum is: ")); // line 12
    main.instructions.add(VMInstr.WRITE()); // line 13
    main.instructions.add(VMInstr.LOAD(0)); // sum - line 14
    main.instructions.add(VMInstr.TOSTR()); // itos(sum) - line 15
    main.instructions.add(VMInstr.WRITE()); // line 16
    main.instructions.add(VMInstr.PUSH("\n")); // line 17
    main.instructions.add(VMInstr.WRITE()); // line 18

    main.instructions.add(VMInstr.PUSH("Goodbye!\n")); // line 19
    main.instructions.add(VMInstr.WRITE()); // line 20
    main.instructions.add(VMInstr.JMP(27)); // line 21

    main.instructions.add(VMInstr.LOAD(0)); // sum - line 22
    main.instructions.add(VMInstr.LOAD(1)); // val - line 23
    main.instructions.add(VMInstr.ADD()); // sum + val - line 24
    main.instructions.add(VMInstr.STORE(0)); // sum  - line 25
    main.instructions.add(VMInstr.JMP(4)); // start of while -- line 26
    main.instructions.add(VMInstr.NOP()); // line 27
    vm.run();
  }
}
