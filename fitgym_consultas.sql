-- ======================================
--      CONSULTAS SQL SIGNIFICATIVAS
-- ======================================

-- ==========================
-- 1. Consultas de usuarios
-- ==========================

-- 1.1 Información completa de un usuario
SELECT * 
FROM USUARIO 
WHERE id = 1;

-- 1.2 Listar todos los clientes
SELECT u.id, u.nombre, u.apellidos, c.edad, c.peso, c.altura
FROM USUARIO u
JOIN CLIENTE c ON u.id = c.id_usuario;

-- 1.3 Listar todos los monitores
SELECT u.id, u.nombre, u.apellidos, m.especialidad, m.telefono_contacto
FROM USUARIO u
JOIN MONITOR m ON u.id = m.id_usuario;

-- 1.4 Buscar usuarios por email o nombre
SELECT * 
FROM USUARIO 
WHERE email LIKE '%@example.com%' 
   OR nombre LIKE 'Ana%';

-- ==========================
-- 2. Consultas de clases y horarios
-- ==========================

-- 2.1 Listar todas las clases
SELECT * 
FROM CLASE;

-- 2.2 Mostrar horarios de una clase específica con instructor y sala
SELECT h.id_horario, h.fecha, h.hora_inicio, h.hora_fin,
       u.nombre || ' ' || u.apellidos AS instructor,
       s.nombre_sala
FROM HORARIO_CLASE h
JOIN MONITOR m ON h.id_instructor = m.id_usuario
JOIN USUARIO u ON m.id_usuario = u.id
JOIN SALA s ON h.id_sala = s.id_sala
WHERE h.id_clase = 1;

-- 2.3 Comprobar disponibilidad de plazas para un horario
SELECT h.id_horario, h.plazas_totales,
       COUNT(r.id_reserva) AS plazas_ocupadas,
       h.plazas_totales - COUNT(r.id_reserva) AS plazas_disponibles
FROM HORARIO_CLASE h
LEFT JOIN RESERVA r ON h.id_horario = r.id_horario AND r.estado = 'reservada'
WHERE h.id_horario = 1
GROUP BY h.id_horario;

-- ==========================
-- 3. Consultas de reservas
-- ==========================

-- 3.1 Obtener reservas de un cliente
SELECT r.id_reserva, c.nombre || ' ' || c.apellidos AS cliente,
       h.fecha, h.hora_inicio, h.hora_fin, cl.nombre AS clase, r.estado
FROM RESERVA r
JOIN CLIENTE c ON r.id_usuario = c.id_usuario
JOIN HORARIO_CLASE h ON r.id_horario = h.id_horario
JOIN CLASE cl ON h.id_clase = cl.id_clase
WHERE c.id_usuario = 1;

-- 3.2 Listar clientes inscritos a un horario
SELECT c.nombre || ' ' || c.apellidos AS cliente
FROM RESERVA r
JOIN CLIENTE c ON r.id_usuario = c.id_usuario
WHERE r.id_horario = 1;

-- ==========================
-- 4. Consultas de actividad
-- ==========================

-- 4.1 Total de minutos entrenados por semana
SELECT SUM(duracion_minutos) AS total_minutos
FROM ACTIVIDAD_USUARIO
WHERE id_usuario = 1
  AND fecha BETWEEN '2026-01-20' AND '2026-01-26';

-- 4.2 Promedio diario de entrenamiento
SELECT AVG(duracion_minutos) AS promedio_diario
FROM ACTIVIDAD_USUARIO
WHERE id_usuario = 1
  AND fecha BETWEEN '2026-01-20' AND '2026-01-26';

-- 4.3 Racha de días consecutivos entrenados
-- Nota: SQLite no tiene función directa, se calcula con fechas consecutivas
WITH dias_entrenados AS (
    SELECT DISTINCT fecha
    FROM ACTIVIDAD_USUARIO
    WHERE id_usuario = 1
)
SELECT COUNT(*) AS racha_actual
FROM dias_entrenados d1
WHERE NOT EXISTS (
    SELECT 1
    FROM dias_entrenados d2
    WHERE julianday(d1.fecha) - 1 = julianday(d2.fecha)
);

-- ==========================
-- 5. Consultas de metas y objetivos
-- ==========================

-- 5.1 Porcentaje de cumplimiento de meta semanal
SELECT o.horas_objetivo,
       SUM(a.duracion_minutos)/60.0 AS horas_entrenadas,
       (SUM(a.duracion_minutos)/60.0 / o.horas_objetivo) * 100 AS porcentaje_cumplimiento
FROM OBJETIVO_SEMANAL o
LEFT JOIN ACTIVIDAD_USUARIO a ON o.id_usuario = a.id_usuario
WHERE o.id_usuario = 1 AND o.semana = 4 AND o.anio = 2026
GROUP BY o.id_objetivo;

-- 5.2 Comparar objetivos vs actividad real (resumen)
SELECT o.semana, o.horas_objetivo, 
       SUM(a.duracion_minutos)/60.0 AS horas_entrenadas,
       (SUM(a.duracion_minutos)/60.0 / o.horas_objetivo) * 100 AS porcentaje_cumplimiento
FROM OBJETIVO_SEMANAL o
LEFT JOIN ACTIVIDAD_USUARIO a 
  ON o.id_usuario = a.id_usuario 
  AND strftime('%W', a.fecha) = o.semana
WHERE o.id_usuario = 1
GROUP BY o.id_objetivo;

-- ==========================
-- 6. Consultas de favoritos y suscripciones
-- ==========================

-- 6.1 Clases favoritas de un cliente
SELECT cl.nombre, cl.descripcion
FROM FAVORITO f
JOIN CLASE cl ON f.id_clase = cl.id_clase
WHERE f.id_usuario = 1;

-- 6.2 Usuarios con suscripción activa
SELECT u.nombre || ' ' || u.apellidos AS usuario, s.tipo, s.fecha_inicio, s.fecha_fin
FROM SUSCRIPCION s
JOIN USUARIO u ON s.id_usuario = u.id
WHERE s.estado = 'activa';
