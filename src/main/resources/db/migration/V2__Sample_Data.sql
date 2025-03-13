-- Sample Users
INSERT INTO users (id, email, password, first_name, last_name, role, address, phone, created_at) VALUES
    ('u1', 'admin@groceries.com', '$2a$10$dL4/BjwNZV4mKU0nGFnzWe6psQIwdE.DOOXkJQpKH8KgV0TM8SZuy', 'Admin', 'User', 'ADMIN', '123 Admin St', '+1234567890', '2025-03-13 08:00:00'),
    ('u2', 'john@example.com', '$2a$10$dL4/BjwNZV4mKU0nGFnzWe6psQIwdE.DOOXkJQpKH8KgV0TM8SZuy', 'John', 'Doe', 'CUSTOMER', '456 Customer Ave', '+1234567891', '2025-03-13 08:00:00'),
    ('u3', 'jane@example.com', '$2a$10$dL4/BjwNZV4mKU0nGFnzWe6psQIwdE.DOOXkJQpKH8KgV0TM8SZuy', 'Jane', 'Smith', 'CUSTOMER', '789 Customer Blvd', '+1234567892', '2025-03-13 08:00:00'),
    ('u4', 'driver1@groceries.com', '$2a$10$dL4/BjwNZV4mKU0nGFnzWe6psQIwdE.DOOXkJQpKH8KgV0TM8SZuy', 'Mike', 'Driver', 'DRIVER', '321 Driver Rd', '+1234567893', '2025-03-13 08:00:00');

-- Sample Products
INSERT INTO products (id, name, description, price, category, quantity, created_at, updated_at) VALUES
    ('p1', 'Organic Bananas', 'Fresh organic bananas, 1 bunch', 2.99, 'FRUITS', 100, '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('p2', 'Whole Milk', 'Fresh whole milk, 1 gallon', 3.99, 'DAIRY', 50, '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('p3', 'Whole Wheat Bread', 'Fresh baked whole wheat bread', 2.49, 'BAKERY', 30, '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('p4', 'Chicken Breast', 'Fresh boneless chicken breast, 1 lb', 5.99, 'MEAT', 40, '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('p5', 'Organic Spinach', 'Fresh organic spinach, 10 oz bag', 3.99, 'VEGETABLES', 60, '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('p6', 'Greek Yogurt', 'Plain Greek yogurt, 32 oz', 4.99, 'DAIRY', 45, '2025-03-13 08:00:00', '2025-03-13 08:00:00');

-- Sample Orders
INSERT INTO orders (id, user_id, status, total_amount, delivery_address, delivery_phone, tracking_number, estimated_delivery_time, delivery_notes, created_at, updated_at) VALUES
    ('o1', 'u2', 'DELIVERED', 15.96, '456 Customer Ave', '+1234567891', 'TRK123456', '2025-03-13 10:00:00', 'Leave at front door', '2025-03-13 08:00:00', '2025-03-13 10:00:00'),
    ('o2', 'u3', 'IN_PROGRESS', 22.94, '789 Customer Blvd', '+1234567892', 'TRK123457', '2025-03-13 16:00:00', 'Ring doorbell', '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('o3', 'u2', 'PENDING', 18.96, '456 Customer Ave', '+1234567891', 'TRK123458', '2025-03-13 17:00:00', 'Call upon arrival', '2025-03-13 08:00:00', '2025-03-13 08:00:00');

-- Sample Order Items
INSERT INTO order_items (id, order_id, product_id, product_name, unit_price, quantity, subtotal) VALUES
    ('oi1', 'o1', 'p1', 'Organic Bananas', 2.99, 2, 5.98),
    ('oi2', 'o1', 'p2', 'Whole Milk', 3.99, 2, 7.98),
    ('oi3', 'o1', 'p3', 'Whole Wheat Bread', 2.49, 1, 2.49),
    ('oi4', 'o2', 'p4', 'Chicken Breast', 5.99, 2, 11.98),
    ('oi5', 'o2', 'p5', 'Organic Spinach', 3.99, 1, 3.99),
    ('oi6', 'o2', 'p6', 'Greek Yogurt', 4.99, 1, 4.99),
    ('oi7', 'o3', 'p2', 'Whole Milk', 3.99, 2, 7.98),
    ('oi8', 'o3', 'p5', 'Organic Spinach', 3.99, 2, 7.98),
    ('oi9', 'o3', 'p3', 'Whole Wheat Bread', 2.49, 1, 2.49);

-- Sample Payments
INSERT INTO payments (id, order_id, user_id, amount, status, created_at, updated_at) VALUES
    ('pay1', 'o1', 'u2', 15.96, 'COMPLETED', '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('pay2', 'o2', 'u3', 22.94, 'COMPLETED', '2025-03-13 08:00:00', '2025-03-13 08:00:00'),
    ('pay3', 'o3', 'u2', 18.96, 'PENDING', '2025-03-13 08:00:00', '2025-03-13 08:00:00');

-- Sample Notifications
INSERT INTO notifications (id, user_id, title, message, type, reference_id, is_read, created_at) VALUES
    ('n1', 'u2', 'Order Delivered', 'Your order #o1 has been delivered', 'ORDER_STATUS', 'o1', false, '2025-03-13 10:00:00'),
    ('n2', 'u3', 'Order in Progress', 'Your order #o2 is being prepared', 'ORDER_STATUS', 'o2', false, '2025-03-13 08:00:00'),
    ('n3', 'u4', 'New Delivery Assignment', 'You have been assigned to deliver order #o2', 'DELIVERY_ASSIGNMENT', 'o2', false, '2025-03-13 08:00:00'),
    ('n4', 'u2', 'Payment Confirmation', 'Payment for order #o3 is pending', 'PAYMENT_STATUS', 'pay3', false, '2025-03-13 08:00:00');
