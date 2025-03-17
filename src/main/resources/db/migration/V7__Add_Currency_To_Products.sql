-- Add currency column to products table with default value 'USD'
ALTER TABLE products ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';

-- Update existing products to use USD currency
UPDATE products SET currency = 'USD' WHERE currency IS NULL;
