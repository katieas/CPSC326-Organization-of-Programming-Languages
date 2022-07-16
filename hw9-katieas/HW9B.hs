{-
   Name: 
   File: HW9B.hs
   Date: Spring 2022
   Desc: 
-}


module HW9B (
  myReverse,
  myLast,
  myInit,
  myMemb,
  myReplace,
  myReplaceAll,
  myElemSum,
  myRemDups,
  myListMax,
  myMergeSort
) where


-- TODO: Implement the following functions USING PATTERN MATCHING. You
-- CANNOT use any if-then-else expressions in your
-- implementations. Again, provide the function types for each
-- function (see HW9A.hs for an example). 

-- (1). myReverse
myReverse :: [a] -> [a]
myReverse [] = []
myReverse (x:xs) = myReverse xs ++ [x]


-- (2). myLast 
myLast :: [a] -> a
myLast [] = error "Empty List"
myLast [x] = x
myLast (_:xs) = myLast xs


-- (3). myInit
myInit :: [a] -> [a]
myInit [] = error "Empty List"
myInit [x] = []
myInit (x:xs) = x : myInit xs 


-- (4). myMemb 
myMemb :: Eq a => a -> [a] -> Bool
myMemb _ [] = False
myMemb n (x:xs) 
  | n == x = True
myMemb n (_:xs) = myMemb n xs 


-- (5). myReplace
myReplace :: Eq a => (a,a) -> [a] -> [a]
myReplace _ [] = []
myReplace (a,b) (x:xs)
  | a == x = [b] ++ myReplace (a,b) xs
myReplace (a,b) (x:xs) = x : myReplace (a,b) xs


-- (6). myReplaceAll
myReplaceAll :: Eq a => [(a,a)] -> [a] -> [a]
myReplaceAll [] ys = ys
myReplaceAll (x:xs) ys = myReplaceAll xs (myReplace x ys)

-- (7). myElemSum
myElemSum :: (Eq a, Num a) => a -> [a] -> a
myElemSum _ [] = 0
myElemSum n (x:xs) 
  | n == x = n + myElemSum n xs
myElemSum n (x:xs) = myElemSum n xs

-- (8). myRemDups
myRemDups :: Eq a => [a] -> [a]
myRemDups [] = []
myRemDups (x:xs) 
  | myMemb x xs = myRemDups xs
myRemDups (x:xs) = [x] ++ myRemDups xs 

-- (9). myListMax
myListMax :: Ord a => [a] -> a
myListMax [] = error "Empty List"
myListMax [x] = x
myListMax (x:xs) 
  | (myListMax xs) > x = myListMax xs
myListMax (x:xs) = x

-- (10). myMergeSort
myMergeSort :: Ord a => [a] -> [a]
myMergeSort [] = []
myMergeSort [x] = [x]
myMergeSort xs =
  let firstHalf xs = take (div (length xs) 2) xs
      secondHalf xs = drop (div (length xs) 2) xs
  in
    merge (myMergeSort (firstHalf xs)) (myMergeSort (secondHalf xs))
  where 
    merge xs [] = xs
    merge [] ys = ys
    merge (x:xs) (y:ys)
      | x <= y = x : merge xs (y:ys)
      | otherwise = y : merge (x:xs) ys

