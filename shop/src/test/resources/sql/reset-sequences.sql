-- перенастройка SEQUENCESEQUENCE для определенных запросов
ALTER TABLE t_orders ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM t_orders);
ALTER TABLE t_products ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM t_products);
ALTER TABLE t_products_in_order ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM t_products_in_order);