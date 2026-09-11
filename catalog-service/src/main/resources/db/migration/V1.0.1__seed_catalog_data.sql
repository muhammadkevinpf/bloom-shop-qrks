INSERT INTO categories (id, name, slug, description, parent_id, image_url, is_active) VALUES
(1, 'Footwear', 'footwear', 'Athletic shoes, sneakers, and casual footwear', NULL, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff', TRUE),
(2, 'Apparel', 'apparel', 'Trendy streetwear, t-shirts, and hoodies', NULL, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518', TRUE),
(3, 'Accessories', 'accessories', 'Hats, bags, and daily essentials', NULL, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30', TRUE),
(4, 'Sneakers', 'sneakers', 'Performance running and lifestyle sneakers', 1, 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a', TRUE),
(5, 'T-Shirts', 't-shirts', 'Organic cotton and graphic tees', 2, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a', TRUE);

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));


INSERT INTO products (id, category_id, name, slug, description, base_price, status, featured) VALUES
('e4a1a673-90d2-4e4f-b671-8b776263e801', 4, 'AirFlex Runner', 'airflex-runner', 
 'Ultra-breathable mesh upper with dynamic cushioning for long-distance comfort.', 129.99, 'ACTIVE', TRUE);

INSERT INTO products (id, category_id, name, slug, description, base_price, status, featured) VALUES
('e4a1a673-90d2-4e4f-b671-8b776263e802', 2, 'Urban Street Hoodie', 'urban-street-hoodie', 
 'Heavyweight fleece hoodie tailored for relaxed everyday streetwear aesthetics.', 79.50, 'ACTIVE', TRUE);

INSERT INTO products (id, category_id, name, slug, description, base_price, status, featured) VALUES
('e4a1a673-90d2-4e4f-b671-8b776263e803', 5, 'Classic Minimalist Tee', 'classic-minimalist-tee', 
 '100% combed organic cotton essential crewneck tee with a soft touch feel.', 34.00, 'ACTIVE', FALSE);

INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment) VALUES
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e801', 'AFR-WHT-41', 'White', '41', 0.00),
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e801', 'AFR-WHT-42', 'White', '42', 0.00),
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e801', 'AFR-BLK-42', 'Black', '42', 5.00);

INSERT INTO product_variants (id, product_id, sku, color, size, price_adjustment) VALUES
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e802', 'USH-GRY-M', 'Heather Grey', 'M', 0.00),
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e802', 'USH-GRY-L', 'Heather Grey', 'L', 0.00),
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e802', 'USH-BLK-L', 'Black', 'L', 0.00);

INSERT INTO product_images (id, product_id, image_url, alt_text, is_primary, display_order) VALUES
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e801', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff', 'AirFlex Runner Main Angle', TRUE, 1),
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e801', 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a', 'AirFlex Runner Side Profile', FALSE, 2);

INSERT INTO product_images (id, product_id, image_url, alt_text, is_primary, display_order) VALUES
(gen_random_uuid(), 'e4a1a673-90d2-4e4f-b671-8b776263e802', 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2', 'Urban Street Hoodie Front', TRUE, 1);