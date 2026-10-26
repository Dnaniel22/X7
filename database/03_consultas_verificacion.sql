-- =====================================================================
--  Pastelería X7 — Paso 3: consultas para comprobar la persistencia
-- =====================================================================
--  Ejecútalas en pgAdmin (Query Tool sobre la base pasteleria_x7)
--  después de usar la aplicación. Sirven como evidencia de que los datos
--  quedan guardados en PostgreSQL y de que las relaciones funcionan.
-- =====================================================================

-- 1. Tablas creadas por Hibernate
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- 2. Claves primarias, foráneas, únicas y checks de cada tabla
SELECT tc.table_name, tc.constraint_name, tc.constraint_type
FROM information_schema.table_constraints tc
WHERE tc.table_schema = 'public'
  AND tc.constraint_type IN ('PRIMARY KEY', 'FOREIGN KEY', 'UNIQUE', 'CHECK')
  AND tc.constraint_name NOT LIKE '%_not_null'
ORDER BY tc.table_name, tc.constraint_type;

-- 3. Cantidad de registros por tabla
SELECT 'usuario'  AS tabla, COUNT(*) AS registros FROM usuario
UNION ALL
SELECT 'producto', COUNT(*) FROM producto
UNION ALL
SELECT 'pedido',   COUNT(*) FROM pedido;

-- 4. Usuarios (la contraseña se omite a propósito)
SELECT id, nombre, email, telefono, direccion, rol
FROM usuario
ORDER BY id;

-- 5. Catálogo de productos
SELECT id, nombre, categoria, etiqueta, precio, disponible
FROM producto
ORDER BY categoria, nombre;

-- 6. Pedidos con su cliente y su producto (JOIN por las claves foráneas)
SELECT pe.codigo,
       pe.tipo,
       u.nombre                                         AS cliente,
       u.email,
       COALESCE(pr.nombre, pe.tipo_evento, 'Personalizado') AS producto_o_evento,
       pe.fecha_entrega,
       COALESCE(pe.monto::text, 'Por confirmar')       AS monto,
       pe.estado
FROM pedido pe
JOIN usuario u        ON u.id  = pe.usuario_id
LEFT JOIN producto pr ON pr.id = pe.producto_id
ORDER BY pe.id DESC;

-- 7. Pedidos por estado
SELECT estado, COUNT(*) AS cantidad
FROM pedido
GROUP BY estado
ORDER BY estado;

-- 8. Clientes y cantidad de pedidos de cada uno (relación usuario 1 ── N pedido)
SELECT u.nombre, u.email, COUNT(pe.id) AS pedidos
FROM usuario u
LEFT JOIN pedido pe ON pe.usuario_id = u.id
WHERE u.rol = 'CLIENTE'
GROUP BY u.id, u.nombre, u.email
ORDER BY pedidos DESC, u.nombre;

-- 9. Productos más pedidos (relación producto 1 ── N pedido)
SELECT pr.nombre, COUNT(pe.id) AS veces_pedido, COALESCE(SUM(pe.monto), 0) AS total_soles
FROM producto pr
LEFT JOIN pedido pe ON pe.producto_id = pr.id
GROUP BY pr.id, pr.nombre
ORDER BY veces_pedido DESC, pr.nombre;

-- 10. Prueba de integridad: PostgreSQL rechaza un pedido de un usuario que no existe.
--     Al ejecutarla debe aparecer el error:
--     insert or update on table "pedido" violates foreign key constraint "fk_pedido_usuario"
-- INSERT INTO pedido (tipo, usuario_id, fecha_entrega, estado, nombre_contacto, telefono, direccion, fecha_registro)
-- VALUES ('PEDIDO', 9999, CURRENT_DATE + 3, 'PENDIENTE', 'Prueba', '987654321', 'Ica', now());
