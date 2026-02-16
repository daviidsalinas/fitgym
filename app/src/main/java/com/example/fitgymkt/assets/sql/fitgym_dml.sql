-- =====================================================
-- DML DE DATOS DE PRUEBA PARA FITGYM
-- =====================================================

-- -------------------------------
-- USUARIOS
-- -------------------------------
INSERT INTO USUARIO (id_usuario, Nombre, Apellidos, Email, Contraseña, FotoPerfil)
VALUES
(1, 'David', 'Salinas', 'david@gmail.com', 'pass123', 'david.jpg'),
(2, 'Pepe', 'Barbera', 'pepe@gmail.com', 'pass456', 'pepe.jpg'),
(3, 'Miguel', 'Lopez', 'miguel@gmail.com', 'pass789', 'miguel.jpg');

-- -------------------------------
-- CLIENTES (Jerarquía)
-- -------------------------------
INSERT INTO CLIENTE (ID_USUARIO, Edad, Peso, Altura)
VALUES
(1, 25, 70, 175),
(2, 30, 80, 180);

-- -------------------------------
-- MONITORES (Jerarquía)
-- -------------------------------
INSERT INTO MONITOR (ID_USUARIO, Especialidad, Telefono_contacto)
VALUES
(3, 'Pilates', '666123456');

-- -------------------------------
-- TELEFONOS USUARIO
-- -------------------------------
INSERT INTO TELEFONO_USUARIO (Id_telefono, id_usuario, Telefono)
VALUES
(1, 1, '600111222'),
(2, 1, '600333444'),
(3, 2, '611555666'),
(4, 3, '622777888');

-- -------------------------------
-- DIRECCIONES USUARIO
-- -------------------------------
INSERT INTO DIRECCION_USUARIO (id_direccion, id_usuario, calle, ciudad, codigo_postal, provincia)
VALUES
(1, 1, 'Calle Falsa 123', 'Madrid', '28001', 'Madrid'),
(2, 2, 'Av. Siempre Viva 456', 'Barcelona', '08002', 'Barcelona'),
(3, 3, 'Paseo de la Reforma 789', 'Valencia', '46001', 'Valencia');

-- -------------------------------
-- CONFIGURACION_USUARIO
-- -------------------------------
INSERT INTO CONFIGURACION_USUARIO (id_configuracion, id_usuario, modo_oscuro, notificaciones, idioma)
VALUES
(1, 1, 1, 1, 'ES'),
(2, 2, 0, 1, 'ES'),
(3, 3, 0, 0, 'EN');

-- -------------------------------
-- SALAS
-- -------------------------------
INSERT INTO SALA (id_sala, nombre_sala, capacidad)
VALUES
(1, 'Sala Principal', 20),
(2, 'Sala 2', 15);

-- -------------------------------
-- CLASES
-- -------------------------------
INSERT INTO CLASE (id_clase, nombre, descripcion, imagen_url)
VALUES
(1, 'Yoga Flow', 'Clase de yoga para relajación y flexibilidad', 'yoga.jpg'),
(2, 'Crossfit', 'Entrenamiento intenso de fuerza y resistencia', 'crossfit.jpg'),
(3, 'Pilates', 'Clase de pilates centrada en el core y postura', 'pilates.jpg');

-- -------------------------------
-- HORARIO_CLASE
-- -------------------------------
INSERT INTO HORARIO_CLASE (id_horario, id_clase, id_monitor, id_sala, fecha, hora_inicio, hora_fin, plazas_totales)
VALUES
(1, 1, 3, 1, '2026-01-25', '08:00', '09:00', 15),
(2, 1, 3, 1, '2026-01-25', '18:00', '19:00', 15),
(3, 2, 3, 2, '2026-01-26', '09:00', '10:00', 15),
(4, 3, 3, 1, '2026-01-26', '10:00', '11:00', 15);

-- -------------------------------
-- RESERVAS
-- -------------------------------
INSERT INTO RESERVA (id_reserva, id_usuario, id_horario, estado, fecha_reserva)
VALUES
(1, 1, 1, 'reservada', '2026-01-20'),
(2, 1, 2, 'completada', '2026-01-21'),
(3, 2, 1, 'reservada', '2026-01-22');

-- -------------------------------
-- ACTIVIDAD_USUARIO
-- -------------------------------
INSERT INTO ACTIVIDAD_USUARIO (id_actividad, id_usuario, fecha, duracion_minutos, id_horario)
VALUES
(1, 1, '2026-01-20', 60, 1),
(2, 1, '2026-01-21', 55, 2),
(3, 2, '2026-01-20', 45, 1),
(4, 2, '2026-01-22', 50, NULL); -- Entrenamiento libre

-- -------------------------------
-- OBJETIVO_SEMANAL
-- -------------------------------
INSERT INTO OBJETIVO_SEMANAL (id_objetivo, id_usuario, horas_objetivo, Semana, anio)
VALUES
(1, 1, 8.0, 4, 2026),
(2, 2, 6.0, 4, 2026);

-- -------------------------------
-- FAVORITO
-- -------------------------------
INSERT INTO FAVORITO (id_favorito, id_usuario, id_clase, Fecha_marcado)
VALUES
(1, 1, 1, '2026-01-18'),
(2, 2, 2, '2026-01-18');

-- -------------------------------
-- SUSCRIPCION
-- -------------------------------
INSERT INTO SUSCRIPCION (id_suscripcion, id_usuario, tipo, fecha_inicio, fecha_fin, estado)
VALUES
(1, 1, 'Mensual', '2026-01-01', '2026-01-31', 'activa'),
(2, 2, 'Trimestral', '2026-01-01', '2026-03-31', 'activa'),
(3, 3, 'Mensual', '2026-01-01', '2026-01-31', 'activa');
