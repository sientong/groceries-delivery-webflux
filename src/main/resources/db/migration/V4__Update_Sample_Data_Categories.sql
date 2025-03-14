-- Update existing products with their new category_id
UPDATE products p
SET category_id = c.id
FROM categories c
WHERE 
    (p.id = 'p1' AND c.name = 'FRUITS') OR
    (p.id IN ('p2', 'p6') AND c.name = 'DAIRY') OR
    (p.id = 'p3' AND c.name = 'BAKERY') OR
    (p.id = 'p4' AND c.name = 'MEAT') OR
    (p.id = 'p5' AND c.name = 'VEGETABLES');
