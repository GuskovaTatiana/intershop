CREATE SEQUENCE IF NOT EXISTS T_ORDERS_ID_SEQ START WITH 4 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS t_products_in_order_id_seq START WITH 8 INCREMENT BY 1;

INSERT INTO t_products (id, title, image_url, description, price)
VALUES
    (21, 'Детский компьютер обучающий', '/images/Child_computer_education.png', 'Детский ноутбук развивающий — отличный выбор для маленьких исследователей. Этот обучающий ноутбук детский станет верным помощником в освоении новых знаний.', 625),
    (22, 'Мозаика для малышей', '/images/Mosaic_for_kids.png', 'Детская крупная мозаика - это развивающая игрушка для детей от 1 до 5 лет, которая состоит из крупных фишек, выполненных в ярких цветах, что делает ее идеальной для малышей и безопасной.', 514),
    (23, 'Пистолет для мыльных пузырей', '/images/Soap_Bubble_Gun.png', 'Детский игрушечный пистолет для мыльных пузырей - это развлечение, которое подарит вашему ребенку множество ярких эмоций, моментов радости и веселья.', 483),
    (24, 'Магнитный конструктор', '/images/Magnetic_Constructor.png', 'Магнитный конструктор для мальчиков и девочек ИГРОЗОНИУМ в подарочной упаковке поможет легко занять малышей игрой на несколько часов. Его легко собирать и разбирать.', 910),
    (25, 'Товар вне коризны', '/images/test.png', 'Минимум описания', 2);

INSERT INTO t_orders (id, status, deleted, created_at, updated_at)
VALUES
    (1, 'CLOSED', false, now(), now()),
    (2, 'IN_PROGRESS', false, now(), now()),
    (3, 'CREATE', false, now(), now());
INSERT INTO t_products_in_order (id, order_id, product_id, product_count)
VALUES
    (1, 3, 22, 1),
    (2, 3, 21, 1),
    (3, 3, 23, 1),
    (4, 3, 24, 4),
    (5, 1, 21, 1),
    (6, 1, 22, 1),
    (7, 2, 23, 3);


