-- Add unit column to products table with default values based on category
ALTER TABLE products ADD COLUMN unit VARCHAR(10) NOT NULL DEFAULT 'piece';

-- Update existing products with appropriate units
UPDATE products SET unit = 'bunch' WHERE id = 'p1';  -- Bananas
UPDATE products SET unit = 'gallon' WHERE id = 'p2';  -- Milk
UPDATE products SET unit = 'loaf' WHERE id = 'p3';  -- Bread
UPDATE products SET unit = 'lb' WHERE id = 'p4';  -- Chicken
UPDATE products SET unit = 'oz' WHERE id = 'p5';  -- Spinach
UPDATE products SET unit = 'oz' WHERE id = 'p6';  -- Yogurt
