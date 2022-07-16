{-
  Name:
  File: main.hs
  Date: Spring 2022
  Desc: Basic tests for HW10. To execute from the command line using
        ghci, run: ghci main.hs -e main
-}

import HW10

-- TODO: Add additional set examples as needed here
s1 = Elem 'a' (Elem 'c' (Elem 'd' (Elem 'b' (Elem 'e' EmptySet))))
s2 = Elem 'a' (Elem 'c' (Elem 'd' (Elem 'b' (Elem 'e' (Elem 'f' EmptySet)))))
s3 = Elem 'a' (Elem 'b' EmptySet)
s4 = Elem 'b' EmptySet
s5 = Elem 'c' EmptySet
s6 = Elem 'a' EmptySet
s7 = Elem 'f' EmptySet
s8 = EmptySet

s9 = Elem 1 (Elem 2 (Elem 3 (Elem 4 (Elem 5 EmptySet))))
s10 = Elem 2 (Elem 4 EmptySet)
s11 = Elem 1 (Elem 3 (Elem 5 EmptySet))
s12 = Elem 5 EmptySet
s13 = Elem 2 (Elem 1 (Elem 4 (Elem 3 (Elem 5 EmptySet))))
s14 = Elem 2 (Elem 4 (Elem 6 EmptySet))



-- TODO: Add additional tests to for hw10 below
main = do
  -- member tests
  putStrLn (assertTrue (member 'a' s1) "member 1")    
  putStrLn (assertTrue (member 'b' s1) "member 2")    
  putStrLn (assertTrue (member 'c' s1) "member 3")    
  putStrLn (assertTrue (member 'd' s1) "member 4")
  putStrLn (assertTrue (member 'e' s1) "member 5")
  putStrLn (assertFalse (member 'f' s1) "member 6")
  -- add tests
  putStrLn (assertEqual s1 (add 'a' s1) "add 1") 
  putStrLn (assertEqual s2 (add 'f' s1) "add 2") 
  putStrLn (assertEqual s14 (add 6 s10) "add 3")
  -- remove tests
  putStrLn (assertEqual s1 (remove 'f' s2) "remove 1")
  putStrLn (assertEqual s4 (remove 'a' s3) "remove 2")
  putStrLn (assertEqual s1 (remove 'g' s1) "remove 3")
  -- size tests
  putStrLn (assertEqual 5 (size s1) "size 1")
  putStrLn (assertEqual 6 (size s2) "size 2")
  putStrLn (assertEqual 0 (size EmptySet) "size 3")
  -- subset tests
  putStrLn (assertTrue (subset s1 s2) "subset 1")  
  putStrLn (assertTrue (subset s4 s3) "subset 2")
  putStrLn (assertFalse (subset s4 s5) "subset 3")   
  -- union tests
  putStrLn (assertEqual s3 (union s6 s4) "union 1")  
  putStrLn (assertEqual s1 (union s3 s1) "union 2")  
  putStrLn (assertEqual s13 (union s10 s11) "union 3") 
  -- intersect tests
  putStrLn (assertEqual s6 (intersect s6 s3) "intersect 1")  
  putStrLn (assertEqual s3 (intersect s1 s3) "intersect 2")  
  putStrLn (assertEqual s1 (intersect s1 s2) "intersect 3")  
  -- difference tests
  putStrLn (assertEqual s6 (difference s3 s4) "difference 1")  
  putStrLn (assertEqual s7 (difference s2 s1) "difference 2")  
  putStrLn (assertEqual s3 (difference s3 s8) "difference 3")  
  -- filter set tests
  putStrLn (assertEqual s10 (filterSet even s9) "filterSet 1")  
  putStrLn (assertEqual s11 (filterSet odd s9) "filterSet 2")  
  putStrLn (assertEqual s12 (filterSet (>4) s9) "filterSet 3")  
  -- to list tests
  putStrLn (assertEqual ['a','c','d','b','e'] (toList s1) "toList 1")  
  putStrLn (assertEqual ['a'] (toList s6) "toList 2")
  putStrLn (assertEqual ['a','b'] (toList s3) "toList 3")  





  
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

