-- Create Categories Table
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add category_id column to products
ALTER TABLE products ADD COLUMN category_id VARCHAR(36);

-- Insert Categories
INSERT INTO categories (id, name) VALUES 
('cat-1', 'FRUITS'),
('cat-2', 'DAIRY'),
('cat-3', 'BAKERY'),
('cat-4', 'MEAT'),
('cat-5', 'VEGETABLES');

-- Update existing products with category_id based on their category name
UPDATE products SET category_id = 'cat-1' WHERE category = 'FRUITS';
UPDATE products SET category_id = 'cat-2' WHERE category = 'DAIRY';
UPDATE products SET category_id = 'cat-3' WHERE category = 'BAKERY';
UPDATE products SET category_id = 'cat-4' WHERE category = 'MEAT';
UPDATE products SET category_id = 'cat-5' WHERE category = 'VEGETABLES';

-- Make category_id NOT NULL and add foreign key constraint
ALTER TABLE products 
    ALTER COLUMN category_id SET NOT NULL,
    ADD CONSTRAINT fk_products_category 
    FOREIGN KEY (category_id) 
    REFERENCES categories(id);

-- Drop old category column
ALTER TABLE products DROP COLUMN category;

-- Add index on category_id
CREATE INDEX idx_products_category ON products(category_id);
