{-
  Name:
  File: test9b.hs
  Date: Spring 2022
  Desc: Basic tests for HW9. To execute from the command line using
        ghci, run: ghci test9a.hs -e main
-}

import HW9B

-- TODO: Add additional tests to for hw9b below
main = do
  -- my reverse tests
  putStrLn (assertTrue (null (myReverse [])) "myReverse 1")    
  putStrLn (assertEqual [1] (myReverse [1]) "myReverse 2")  
  putStrLn (assertEqual [2,1] (myReverse [1,2]) "myReverse 3")
  putStrLn (assertEqual ['a'..'z'] (myReverse ['z','y'..'a']) "myReverse 4")
  -- my last tests
  putStrLn (assertEqual 1 (myLast [1]) "myLast 1")
  putStrLn (assertEqual 2 (myLast [1,2]) "myLast 2")
  putStrLn (assertEqual 3 (myLast [1,2,3]) "myLast 3")
   -- my init tests
  putStrLn (assertEqual [1,2] (myInit [1,2,3]) "myInit 1")
  putStrLn (assertEqual [] (myInit [1]) "myInit 2")
  putStrLn (assertEqual [1,2,3,4] (myInit [1,2,3,4,5]) "myInit 3")
  -- my memb tests
  putStrLn (assertEqual True (myMemb 3 [1,2,3,4]) "myMemb 1")
  putStrLn (assertEqual False (myMemb 3 [1,2,4,5]) "myMemb 2")
  putStrLn (assertEqual True (myMemb 5 [1,2,4,5,6]) "myMemb 3")
  -- my replace tests
  putStrLn (assertEqual [1,8,3,8] (myReplace (2,8) [1,2,3,2]) "myReplace 1")
  putStrLn (assertEqual [2,2,2,2] (myReplace (1,2) [1,2,1,2]) "myReplace 2")
  putStrLn (assertEqual [3,3,4,4] (myReplace (2,3) [2,2,4,4]) "myReplace 3")
  -- my replace all tests
  putStrLn (assertEqual [1,8,8,8] (myReplaceAll [(2,8), (3,8)] [1,2,3,2]) "myReplaceAll 1")
  putStrLn (assertEqual ['b','b','d','d'] (myReplaceAll [('a','b'), ('c','d')] ['a','b','c','d']) "myReplaceAll 2")
  putStrLn (assertEqual [3,3,3,4] (myReplaceAll [(1,2), (2,3)] [1,2,3,4]) "myReplaceAll 3")
  -- my elem sum tests
  putStrLn (assertEqual 10 (myElemSum 10 [15,10,25]) "myElemSum 1")
  putStrLn (assertEqual 12 (myElemSum 3 [3,2,3,2,3,4,3]) "myElemSum 2")
  putStrLn (assertEqual 0 (myElemSum 3 []) "myElemSum 3")
  -- my rem dups tests
  putStrLn (assertEqual ['c','b','a'] (myRemDups ['a','b','a','c','b','a']) "myRemDups 1")
  putStrLn (assertEqual [10,13,11,12] (myRemDups [10,11,13,11,12]) "myRemDups 2")
  putStrLn (assertEqual [5,10] (myRemDups [10,10,10,5,10]) "myRemDups 3")
  -- my list max tests 
  putStrLn (assertEqual 12 (myListMax [7,1,9,12,10]) "myListMax 1")
  putStrLn (assertEqual 5 (myListMax [1,2,3,4,5]) "myListMax 2")
  putStrLn (assertEqual 99 (myListMax [99,5,6,70,4]) "myListMax 3")
  -- my merge sort tests
  putStrLn (assertEqual [1,2,3,4,5] (myMergeSort [5,4,3,2,1]) "myMergeSort 1")
  putStrLn (assertEqual [1,7,9,10,12] (myMergeSort [7,1,9,12,10]) "myMergeSort 2")
  putStrLn (assertEqual [4,5,9,20,30] (myMergeSort [20,30,5,4,9]) "myMergeSort 3")


assertEqual :: (Show a, Eq a) => a -> a -> String -> String
assertEqual x y s =
  if x == y then
    s ++ " [Pass]"
  else
    s ++ " [Fail] ... expecting " ++ (show x) ++ " found " ++ (show y)


assertTrue :: Bool -> String -> String
assertTrue x s = assertEqual True x s


assertFalse :: Bool -> String -> String
assertFalse x s = assertEqual False x s

