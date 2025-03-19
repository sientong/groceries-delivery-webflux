-- Clear existing data
DELETE FROM products;
DELETE FROM categories;

-- Insert test categories
INSERT INTO categories (id, name, display_name, created_at, updated_at)
VALUES 
    ('fruits', 'fruits', 'Fruits', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('vegetables', 'vegetables', 'Vegetables', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test products
INSERT INTO products (id, name, description, price_amount, price_currency, category_id, quantity_value, quantity_unit, created_at, updated_at)
VALUES 
    ('test-apple', 'Test Apple', 'Fresh test apple', 5.99, 'USD', 'fruits', 100, 'kg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('test-banana', 'Test Banana', 'Fresh test banana', 3.99, 'USD', 'fruits', 150, 'kg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
