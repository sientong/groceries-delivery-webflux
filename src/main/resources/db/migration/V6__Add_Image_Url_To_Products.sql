-- Add image_url column
ALTER TABLE products
ADD COLUMN image_url VARCHAR(255);

-- Split category into id and name
ALTER TABLE products
ADD COLUMN category_name VARCHAR(100);

-- Copy existing category data to category_name
UPDATE products
SET category_name = (SELECT name FROM categories WHERE id = category_id)
WHERE category_id IS NOT NULL;

-- Generate UUIDs for category_id
UPDATE products
SET category_id = gen_random_uuid()::text
WHERE category_id IS NULL;
