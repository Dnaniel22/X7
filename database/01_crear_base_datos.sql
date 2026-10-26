-- =====================================================================
--  Pastelería X7 — Paso 1: crear la base de datos en PostgreSQL
-- =====================================================================
--  En pgAdmin:
--    1. Conéctate al servidor (PostgreSQL 15/16/17, puerto 5432).
--    2. Clic derecho en "Databases" → Create → Database...  y escribe: pasteleria_x7
--       (o abre el Query Tool sobre la base "postgres" y ejecuta este script).
--
--  No hace falta crear las tablas a mano: al iniciar la aplicación,
--  Hibernate las crea con spring.jpa.hibernate.ddl-auto=update.
-- =====================================================================

CREATE DATABASE pasteleria_x7
    WITH ENCODING = 'UTF8'
    TEMPLATE = template0;

COMMENT ON DATABASE pasteleria_x7 IS 'Sistema Web de Gestión de Pedidos y Catálogo — Pastelería y Catering X7';
