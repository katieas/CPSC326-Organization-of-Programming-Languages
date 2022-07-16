{-
  Name:
  File: main.hs
  Date: Spring 2022
  Desc: Basic tests for HW8. To execute from the command line using
        ghci, run: ghci main.hs -e main
-}

import HW8


-- TODO: Add additional tests to main below

main = do
  putStrLn (assertEqual 1 (myMin 1 2) "myMinTest 1")
  putStrLn (assertEqual 1 (myMin 2 1) "myMinTest 2")
  putStrLn (assertEqual 1 (myMin 1 1) "myMinTest 3")  

  putStrLn (assertEqual 2 (myMedian 1 2 3) "myMedian 1")  
  putStrLn (assertEqual 2 (myMedian 3 2 1) "myMedian 2")  
  putStrLn (assertEqual 2 (myMedian 2 3 2) "myMedian 3")  
  putStrLn (assertEqual 2 (myMedian 2 2 2) "myMedian 4") 
  putStrLn (assertEqual 2 (myMedian 2 1 2) "myMedian 5")   
  putStrLn (assertEqual 5 (myMedian 2 5 10) "myMedian 6")  

  putStrLn (assertEqual (2.0,3.0) (myMidpoint (1.0,1.0) (3.0,5.0)) "myMidpoint 1") 
  putStrLn (assertEqual (3.0,4.5) (myMidpoint (1.0,1.0) (5.0,8.0)) "myMidpoint 2")  

  putStrLn (assertEqual 5.0 (myManhattanDistance (1.0,1.0) (3.0,4.0)) "myManhattanDistance 1") 
  putStrLn (assertEqual 5.0 (myManhattanDistance (3.0,1.0) (1.0,4.0)) "myManhattanDistance 2") 

  putStrLn (assertEqual 5.0 (myEuclideanDistance (1.0,1.0) (4.0,5.0)) "myEuclideanDistance 1") 
  putStrLn (assertEqual 5.0 (myEuclideanDistance (2.0,(-1.0)) ((-2.0),2.0)) "myEuclideanDistance 2") 

  putStrLn (assertEqual 5 (myRangeSum 2 3) "myRangeSum 1")
  putStrLn (assertEqual 2 (myRangeSum 2 2) "myRangeSum 2")
  putStrLn (assertEqual 9 (myRangeSum 2 4) "myRangeSum 3")  
  putStrLn (assertEqual 0 (myRangeSum 2 1) "myRangeSum 4")

  putStrLn (assertEqual 1 (myFib 1) "myFib 1")
  putStrLn (assertEqual 1 (myFib 2) "myFib 2")
  putStrLn (assertEqual 2 (myFib 3) "myFib 3")
  putStrLn (assertEqual 3 (myFib 4) "myFib 4")
  putStrLn (assertEqual 0 (myFib 0) "myFib 5")

  putStrLn (assertEqual 6 (myGCD 6 0) "myGCD 1")
  putStrLn (assertEqual 7 (myGCD 0 7) "myGCD 2")
  putStrLn (assertEqual 6 (myGCD 270 192) "myGCD 3")

  putStrLn (assertEqual True (myEven 0) "myEven 1")
  putStrLn (assertEqual False (myEven 5) "myEven 2")
  putStrLn (assertEqual True (myEven 10) "myEven 3")
  putStrLn (assertEqual False (myEven (-8)) "myEven 4")

  putStrLn (assertEqual True (myOdd 1) "myOdd 1")
  putStrLn (assertEqual False (myOdd 0) "myOdd 2")
  putStrLn (assertEqual True (myOdd 9) "myOdd 3")
  putStrLn (assertEqual False (myOdd (-8)) "myOdd 4")

assertEqual :: (Show a, Eq a) => a -> a -> String -> String
assertEqual x y s =
  if x == y then
    s ++ " [Pass]"
  else
    s ++ " [Fail] ... expecting " ++ (show x) ++ " found " ++ (show y)


