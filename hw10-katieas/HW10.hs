{-
   Name: 
   File: HW10.hs
   Date: Spring 2022
   Desc: Set ADT implementation
-}


module HW10 (
  Set (Elem, EmptySet),
  member,
  add,
  remove,
  size,
  subset,
  union,
  intersect,
  difference,
  filterSet,
  toList
) where

data Set a = Elem a (Set a)
           | EmptySet
           deriving (Show, Eq)

-- TODO: Implement each of the functions below. For each function
-- include the function's type. The type of the member function (as well
-- as a "stubbed out" implementation) is given below as an
-- example. You are NOT allowed to use any if-then-else expressions in
-- your implementation. See the homework assignment for additional
-- information and restrictions.

-- (1). member
member :: Eq a => a -> Set a -> Bool
member _ EmptySet = False
member x (Elem y s)
  | x == y = True
  | otherwise = member x s

-- (2). add
add :: Eq a => a -> Set a -> Set a
add x EmptySet = Elem x EmptySet
add x (Elem y s)
  | x == y = Elem y s
  | otherwise = Elem y (add x s)

-- (3). remove
remove :: Eq a => a -> Set a -> Set a
remove _ EmptySet = EmptySet
remove x (Elem y s)
  | x == y = s
  | otherwise = Elem y (remove x s)

-- (4). size
size :: Set a -> Int
size EmptySet = 0
size (Elem _ s) = 1 + size s

-- (5). subset
subset :: Eq a => Set a -> Set a -> Bool
subset EmptySet _ = True
subset _ EmptySet = False
subset (Elem x s1) (Elem y s2)
  | x == y = subset s1 s2
  | otherwise = subset (Elem x s1) s2

-- (6). union
union :: Eq a => Set a -> Set a -> Set a
union s1 EmptySet = s1
union EmptySet s2 = s2
union (Elem x s1) (Elem y s2)
  | member x s2 = union s1 s2
  | member y s1 = union s1 s2
  | otherwise = Elem x (union (Elem y s2) s1)

-- (7). intersect
intersect :: Eq a => Set a -> Set a -> Set a
intersect EmptySet _ = EmptySet
intersect (Elem x s1) s2
  | member x s2 = Elem x (intersect s1 s2)
  | otherwise = intersect s1 s2

-- (8). difference
difference :: Eq a => Set a -> Set a -> Set a
difference EmptySet _ = EmptySet
difference (Elem x s1) s2
  | member x s2 = difference s1 s2
  | otherwise = Elem x (difference s1 s2)

-- (9). filterSet
filterSet :: (a -> Bool) -> Set a -> Set a
filterSet _ EmptySet = EmptySet
filterSet f (Elem x s)
  | f x = Elem x (filterSet f s)
  | otherwise = filterSet f s

-- (10). toList
toList :: Set a -> [a]
toList EmptySet = []
toList (Elem x s) = x : (toList s)

