{-
   Name: 
   File: HW9A.hs
   Date: Spring 2022
   Desc: 
-}


module HW9A (
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


-- TODO: Implement the following functions WITHOUT USING pattern
-- matching, and instead using if-then-else. For each function,
-- provide the function type. An example is provided for (1), however,
-- the actual definition is still required. 


-- (1). myReverse
myReverse :: [a] -> [a]
myReverse xs = 
  if null xs then 
    []
  else 
    myReverse (tail xs) ++ [head xs]

-- (2). myLast 
myLast :: [a] -> a
myLast xs =
  if null xs then 
    error "Empty List"
  else 
    if length xs == 1 then 
      head xs 
    else 
      myLast (tail xs)

-- (3). myInit
myInit :: [a] -> [a]
myInit xs = 
  if null xs then 
    error "Empty List"
  else 
    take (length xs - 1) xs

-- (4). myMemb
myMemb :: Eq a => a -> [a] -> Bool
myMemb n xs = 
  if null xs then 
    False 
  else 
    if ((head xs) == n) then 
      True
    else 
      myMemb n (tail xs)

-- (5). myReplace
myReplace :: Eq a => (a,a) -> [a] -> [a]
myReplace (a,b) xs =
  if null xs then 
    xs
  else 
    if a == (head xs) then 
      [b] ++ myReplace (a, b) (tail xs)
    else 
      (head xs) : myReplace (a,b) (tail xs)


-- (6). myReplaceAll
myReplaceAll :: Eq a => [(a,a)] -> [a] -> [a]
myReplaceAll xs ys = 
  if null xs then 
    ys
  else 
    myReplaceAll (tail xs) (myReplace (head xs) ys)

-- (7). myElemSum
myElemSum :: (Eq a, Num a) => a -> [a] -> a
myElemSum n xs =
  let sum = 0
  in if null xs then 
    sum 
  else 
    if ((head xs) == n) then 
      (sum + n) + myElemSum n (tail xs)
    else 
      myElemSum n (tail xs)

-- (8). myRemDups
myRemDups :: Eq a => [a] -> [a]
myRemDups xs =
  if null xs then 
    xs
  else 
    if myMemb (head xs) (tail xs) then 
      myRemDups (tail xs)
    else 
      [head xs] ++ myRemDups (tail xs)

-- (9). myListMax
myListMax :: Ord a => [a] -> a
myListMax xs = 
  if null xs then 
    error "Empty List"
  else 
    if (length xs) == 1 then 
      (head xs)
    else
      let max = myListMax (tail xs) 
      in 
        if max > (head xs) then 
          max
        else 
          (head xs)


-- (10). myMergeSort
myMergeSort :: Ord a => [a] -> [a]
myMergeSort xs = 
  if null xs then 
    xs
  else 
    if (length xs) == 1 then 
      xs
    else 
      let firstHalf xs = take (div (length xs) 2) xs
          secondHalf xs = drop (div (length xs) 2) xs
      in
        merge (myMergeSort (firstHalf xs)) (myMergeSort (secondHalf xs))
  where
  merge xs ys =
    if null xs then 
      ys
    else 
      if null ys then 
        xs
      else 
        if (head xs) <= (head ys) then 
          (head xs) : merge (tail xs) ys
        else (head ys) : merge xs (tail ys)
