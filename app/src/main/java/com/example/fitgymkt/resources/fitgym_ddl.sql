PRAGMA foreign_keys = ON;

-- =====================
-- USUARIO
-- =====================
CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellidos TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    contraseña TEXT NOT NULL,
    fotoPerfil TEXT
);

-- =====================
-- CLIENTE (herencia total y solapada)
-- =====================
CREATE TABLE cliente (
    id_usuario INTEGER PRIMARY KEY,
    edad INTEGER,
    peso REAL,       -- kg
    altura REAL,     -- cm
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

-- =====================
-- MONITOR (herencia total y solapada)
-- =====================
CREATE TABLE monitor (
    id_usuario INTEGER PRIMARY KEY,
    especialidad TEXT,
    telefono_contacto TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

-- =====================
-- TELEFONO_USUARIO
-- =====================
CREATE TABLE telefono_usuario (
    id_telefono INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    telefono TEXT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

-- =====================
-- DIRECCION_USUARIO
-- =====================
CREATE TABLE direccion_usuario (
    id_direccion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    calle TEXT,
    ciudad TEXT,
    codigo_postal TEXT,
    provincia TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

-- =====================
-- CLASE
-- =====================
CREATE TABLE clase (
    id_clase INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    imagen_url TEXT
);

-- =====================
-- SALA
-- =====================
CREATE TABLE sala (
    id_sala INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_sala TEXT NOT NULL,
    capacidad INTEGER NOT NULL
);

-- =====================
-- HORARIO_CLASE
-- OJO: id_monitor apunta a monitor.id_usuario
-- =====================
CREATE TABLE horario_clase (
    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_clase INTEGER NOT NULL,
    id_monitor INTEGER NOT NULL,
    id_sala INTEGER NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    plazas_totales INTEGER NOT NULL,
    FOREIGN KEY (id_clase) REFERENCES clase(id_clase),
    FOREIGN KEY (id_monitor) REFERENCES monitor(id_usuario),
    FOREIGN KEY (id_sala) REFERENCES sala(id_sala)
);

-- =====================
-- RESERVA (N:M Cliente - Horario)
-- =====================
CREATE TABLE reserva (
    id_reserva INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,   -- cliente
    id_horario INTEGER NOT NULL,
    estado TEXT CHECK(estado IN ('reservada','completada','cancelada')) NOT NULL,
    fecha_reserva DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES cliente(id_usuario),
    FOREIGN KEY (id_horario) REFERENCES horario_clase(id_horario),
    UNIQUE (id_usuario, id_horario)
);

-- =====================
-- ACTIVIDAD_USUARIO
-- =====================
CREATE TABLE actividad_usuario (
    id_actividad INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    fecha DATE NOT NULL,
    duracion_minutos INTEGER NOT NULL,
    id_horario INTEGER,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_horario) REFERENCES horario_clase(id_horario)
);

-- =====================
-- OBJETIVO_SEMANAL
-- =====================
CREATE TABLE objetivo_semanal (
    id_objetivo INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    horas_objetivo REAL NOT NULL,
    semana INTEGER NOT NULL,
    anio INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    UNIQUE (id_usuario, semana, anio)
);

-- =====================
-- FAVORITO (N:M Cliente - Clase)
-- =====================
CREATE TABLE favorito (
    id_favorito INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    id_clase INTEGER NOT NULL,
    fecha_marcado DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES cliente(id_usuario),
    FOREIGN KEY (id_clase) REFERENCES clase(id_clase),
    UNIQUE (id_usuario, id_clase)
);

-- =====================
-- SUSCRIPCION
-- =====================
CREATE TABLE suscripcion (
    id_suscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- =====================
-- CONFIGURACION_USUARIO
-- =====================
CREATE TABLE configuracion_usuario (
    id_configuracion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL UNIQUE,
    modo_oscuro BOOLEAN,
    notificaciones BOOLEAN,
    idioma TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);
