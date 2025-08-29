DROP SEQUENCE IF EXISTS T_ORDERS_ID_SEQ;
DROP SEQUENCE IF EXISTS t_products_in_order_id_seq;
DELETE FROM t_products_in_order;
delete FROM t_products where id in (21, 22, 23,24);
DELETE FROM t_orders;

