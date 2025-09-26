-- =====================================================
-- TELCONOVA SUPPORTSUITE - SCRIPT DE BASE DE DATOS
-- Feature 3: Seguimiento de Órdenes en Proceso
-- PostgreSQL 15+
-- =====================================================

-- Crear esquema principal
CREATE SCHEMA IF NOT EXISTS telconova;
SET search_path TO telconova;

-- =====================================================
-- TIPOS ENUM PERSONALIZADOS
-- =====================================================

-- Roles de usuario
CREATE TYPE rol_usuario AS ENUM (
    'TECNICO',
    'ADMIN'
);

-- =====================================================
-- TABLA: USUARIOS
-- =====================================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    rol rol_usuario NOT NULL DEFAULT 'TECNICO',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Constraint: Solo emails de @telconova.com
ALTER TABLE usuarios ADD CONSTRAINT chk_email_telconova
CHECK (email LIKE '%@telconova.com');

-- =====================================================
-- TABLA: MATERIALES
-- =====================================================
CREATE TABLE materiales (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    unidad_medida VARCHAR(50) NOT NULL,
    precio_unitario DECIMAL(10,2),
    stock_disponible INTEGER DEFAULT 0 CHECK (stock_disponible >= 0),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- TABLA: ORDENES_TRABAJO
-- =====================================================
CREATE TABLE ordenes_trabajo (
    id BIGSERIAL PRIMARY KEY,
    numero_orden VARCHAR(50) NOT NULL UNIQUE,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(15) NOT NULL DEFAULT 'ASIGNADA',
    prioridad VARCHAR(15) NOT NULL DEFAULT 'MEDIA',
    tipo_servicio VARCHAR(100) NOT NULL,
    cliente_nombre VARCHAR(255) NOT NULL,
    cliente_telefono VARCHAR(20),
    direccion TEXT NOT NULL,
    tecnico_asignado_id BIGINT,
    fecha_asignacion TIMESTAMP,
    fecha_inicio_trabajo TIMESTAMP,
    fecha_fin_trabajo TIMESTAMP,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_orden_tecnico FOREIGN KEY (tecnico_asignado_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,

    -- Constraints de negocio
    CONSTRAINT chk_fechas_trabajo CHECK (
        fecha_fin_trabajo IS NULL OR fecha_inicio_trabajo IS NULL OR
        fecha_fin_trabajo > fecha_inicio_trabajo
    ),
    CONSTRAINT chk_telefono_formato CHECK (
        cliente_telefono IS NULL OR cliente_telefono ~ '^\+?[0-9\s\-\(\)]{7,20}$'
    )
);

-- =====================================================
-- TABLA: EVIDENCIAS
-- =====================================================
CREATE TABLE evidencias (
    id BIGSERIAL PRIMARY KEY,
    orden_trabajo_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    contenido TEXT,
    ruta_archivo VARCHAR(500),
    nombre_archivo_original VARCHAR(255),
    tipo_mime VARCHAR(100),
    tamano_archivo BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creado_por BIGINT NOT NULL,

    -- Foreign Keys
    CONSTRAINT fk_evidencia_orden FOREIGN KEY (orden_trabajo_id)
        REFERENCES ordenes_trabajo(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidencia_usuario FOREIGN KEY (creado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    -- Constraints de negocio
    CONSTRAINT chk_evidencia_contenido CHECK (
        (tipo = 'COMENTARIO' AND contenido IS NOT NULL AND LENGTH(contenido) <= 500) OR
        (tipo = 'FOTO' AND ruta_archivo IS NOT NULL)
    ),
    CONSTRAINT chk_foto_tipo_mime CHECK (
        tipo != 'FOTO' OR tipo_mime IN ('image/jpeg', 'image/jpg', 'image/png')
    ),
    CONSTRAINT chk_tamano_archivo CHECK (
        tamano_archivo IS NULL OR tamano_archivo > 0
    )
);

-- =====================================================
-- TABLA: MATERIALES_UTILIZADOS
-- =====================================================
CREATE TABLE materiales_utilizados (
    id BIGSERIAL PRIMARY KEY,
    orden_trabajo_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    cantidad_utilizada INTEGER NOT NULL CHECK (cantidad_utilizada > 0),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    registrado_por BIGINT NOT NULL,
	codigo_material VARCHAR(50),
	nombre_material VARCHAR(255),
	unidad_medida VARCHAR(50),
	precio_unitario DECIMAL(10,2),


    -- Foreign Keys
    CONSTRAINT fk_mat_util_orden FOREIGN KEY (orden_trabajo_id)
        REFERENCES ordenes_trabajo(id) ON DELETE CASCADE,
    CONSTRAINT fk_mat_util_material FOREIGN KEY (material_id)
        REFERENCES materiales(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mat_util_usuario FOREIGN KEY (registrado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    -- Constraint único por orden y material
    CONSTRAINT uk_orden_material UNIQUE (orden_trabajo_id, material_id)
);

-- =====================================================
-- TABLA: HISTORIAL_ESTADOS
-- =====================================================
CREATE TABLE historial_estados (
    id BIGSERIAL PRIMARY KEY,
    orden_trabajo_id BIGINT NOT NULL,
    estado_anterior VARCHAR,
    estado_nuevo VARCHAR NOT NULL,
    observaciones TEXT,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cambiado_por BIGINT NOT NULL,

    -- Foreign Keys
    CONSTRAINT fk_historial_orden FOREIGN KEY (orden_trabajo_id)
        REFERENCES ordenes_trabajo(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (cambiado_por)
        REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- =====================================================
-- ÍNDICES PARA RENDIMIENTO
-- =====================================================

-- Índices para usuarios
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol_activo ON usuarios(rol, activo);

-- Índices para órdenes de trabajo
CREATE INDEX idx_ordenes_estado ON ordenes_trabajo(estado);
CREATE INDEX idx_ordenes_tecnico ON ordenes_trabajo(tecnico_asignado_id);
CREATE INDEX idx_ordenes_prioridad ON ordenes_trabajo(prioridad);
CREATE INDEX idx_ordenes_fecha_creacion ON ordenes_trabajo(fecha_creacion);
CREATE INDEX idx_ordenes_numero ON ordenes_trabajo(numero_orden);

-- Índices para evidencias
CREATE INDEX idx_evidencias_orden ON evidencias(orden_trabajo_id);
CREATE INDEX idx_evidencias_tipo ON evidencias(tipo);
CREATE INDEX idx_evidencias_fecha ON evidencias(fecha_creacion);

-- Índices para materiales
CREATE INDEX idx_materiales_codigo ON materiales(codigo);
CREATE INDEX idx_materiales_nombre ON materiales(nombre);
CREATE INDEX idx_materiales_activo ON materiales(activo);

-- Índices para materiales utilizados
CREATE INDEX idx_mat_util_orden ON materiales_utilizados(orden_trabajo_id);
CREATE INDEX idx_mat_util_material ON materiales_utilizados(material_id);

-- Índices para historial
CREATE INDEX idx_historial_orden ON historial_estados(orden_trabajo_id);
CREATE INDEX idx_historial_fecha ON historial_estados(fecha_cambio);

-- =====================================================
-- TRIGGERS PARA AUDITORÍA
-- =====================================================

-- Función para actualizar fecha_actualizacion
CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para usuarios
CREATE TRIGGER tr_usuarios_actualizacion
    BEFORE UPDATE ON telconova.usuarios
    FOR EACH ROW
    EXECUTE FUNCTION telconova.actualizar_fecha_modificacion();

-- Trigger para órdenes de trabajo
CREATE TRIGGER tr_ordenes_actualizacion
    BEFORE UPDATE ON telconova.ordenes_trabajo
    FOR EACH ROW
    EXECUTE FUNCTION telconova.actualizar_fecha_modificacion();

-- =====================================================
-- FUNCIÓN Y TRIGGER PARA HISTORIAL DE ESTADOS
-- =====================================================

-- Función para registrar cambios de estado
CREATE OR REPLACE FUNCTION telconova.registrar_cambio_estado()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo registrar si el estado cambió
    IF OLD.estado IS DISTINCT FROM NEW.estado THEN
        INSERT INTO telconova.historial_estados (  -- ← ESPECIFICAR ESQUEMA EXPLÍCITAMENTE
            orden_trabajo_id,
            estado_anterior,
            estado_nuevo,
            observaciones,
            cambiado_por
        ) VALUES (
            NEW.id,
            OLD.estado,
            NEW.estado,
            'Cambio automático de estado',
            COALESCE(NEW.tecnico_asignado_id, 1)
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para cambios de estado
CREATE TRIGGER tr_registrar_cambio_estado
    AFTER UPDATE ON telconova.ordenes_trabajo
    FOR EACH ROW
    EXECUTE FUNCTION telconova.registrar_cambio_estado();

-- =====================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================

-- Procedimiento para buscar materiales por nombre
CREATE OR REPLACE FUNCTION buscar_materiales_por_nombre(
    p_nombre_busqueda VARCHAR(255),
    p_limite INTEGER DEFAULT 10
)
RETURNS TABLE (
    id BIGINT,
    codigo VARCHAR(50),
    nombre VARCHAR(255),
    descripcion TEXT,
    unidad_medida VARCHAR(50),
    stock_disponible INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        m.id,
        m.codigo,
        m.nombre,
        m.descripcion,
        m.unidad_medida,
        m.stock_disponible
    FROM materiales m
    WHERE m.activo = TRUE
      AND UPPER(m.nombre) LIKE UPPER('%' || p_nombre_busqueda || '%')
    ORDER BY m.nombre
    LIMIT p_limite;
END;
$$ LANGUAGE plpgsql;

-- Procedimiento para obtener órdenes por técnico
CREATE OR REPLACE FUNCTION obtener_ordenes_por_tecnico(
    p_tecnico_id BIGINT
)
RETURNS TABLE (
    id BIGINT,
    numero_orden VARCHAR(50),
    titulo VARCHAR(255),
    estado VARCHAR(15),
    prioridad VARCHAR(15),
    cliente_nombre VARCHAR(255),
    fecha_asignacion TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ot.id,
        ot.numero_orden,
        ot.titulo,
        ot.estado,
        ot.prioridad,
        ot.cliente_nombre,
        ot.fecha_asignacion
    FROM ordenes_trabajo ot
    WHERE ot.tecnico_asignado_id = p_tecnico_id
      AND ot.estado IN ('ASIGNADA', 'EN_PROCESO', 'PAUSADA')
    ORDER BY
        CASE ot.prioridad
            WHEN 'CRITICA' THEN 1
            WHEN 'ALTA' THEN 2
            WHEN 'MEDIA' THEN 3
            WHEN 'BAJA' THEN 4
        END,
        ot.fecha_creacion;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- DATOS DE PRUEBA
-- =====================================================

-- Insertar usuarios de prueba
INSERT INTO usuarios (email, contrasena, nombre_completo, rol) VALUES
('admin@telconova.com', '$2a$12$kQoooESW6ZfERmWqBvGtK.2Sy0oZMAZ9tRE2jYIdmd45qK9da9/.y', 'Administrador Sistema', 'ADMIN'), -- password: admin123
('juan.perez@telconova.com', '$2a$12$q0x0qNQyl6CEGHcV8a7NWu53frF29JAgRw2baUG4HaLrq7VQMdvdO', 'Juan Pérez González', 'TECNICO'), -- password: admin123
('maria.rodriguez@telconova.com', '$2a$12$q0x0qNQyl6CEGHcV8a7NWu53frF29JAgRw2baUG4HaLrq7VQMdvdO', 'María Rodríguez López', 'TECNICO'), -- password: admin123
('carlos.martinez@telconova.com', '$2a$12$q0x0qNQyl6CEGHcV8a7NWu53frF29JAgRw2baUG4HaLrq7VQMdvdO', 'Carlos Martínez Silva', 'TECNICO'); -- password: admin123

-- Insertar materiales de prueba
INSERT INTO materiales (codigo, nombre, descripcion, unidad_medida, precio_unitario, stock_disponible) VALUES
('CAB-UTP-001', 'Cable UTP Categoría 6', 'Cable de red UTP Cat 6 para instalaciones de red', 'metros', 2.50, 1000),
('CAB-FIB-001', 'Cable de Fibra Óptica', 'Cable fibra óptica monomodo para larga distancia', 'metros', 8.75, 500),
('CON-RJ45-001', 'Conector RJ45', 'Conector RJ45 para cable UTP', 'unidades', 0.35, 2000),
('ROT-WIF-001', 'Router WiFi 6', 'Router inalámbrico de alta velocidad WiFi 6', 'unidades', 89.99, 50),
('MOD-DSL-001', 'Módem DSL', 'Módem ADSL2+ para conexiones de banda ancha', 'unidades', 45.50, 75),
('ANT-SEC-001', 'Antena Sectorial', 'Antena sectorial 2.4GHz para enlaces punto-multipunto', 'unidades', 125.00, 25),
('CAJ-DIS-001', 'Caja de Distribución', 'Caja para distribución de señales de fibra óptica', 'unidades', 22.80, 100),
('HER-CRI-001', 'Herramienta de Crimpeo', 'Pinza para crimpar conectores RJ45', 'unidades', 18.90, 15);

-- Insertar órdenes de trabajo de prueba
INSERT INTO ordenes_trabajo (numero_orden, titulo, descripcion, estado, prioridad, tipo_servicio, cliente_nombre, cliente_telefono, direccion, tecnico_asignado_id, fecha_asignacion) VALUES
('ORD-2025-001', 'Instalación Internet Residencial', 'Instalación de servicio de internet de 100Mbps para cliente residencial', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Ana García Ruiz', '+57 300 1234567', 'Carrera 70 #45-23, Medellín, Antioquia', 2, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('ORD-2025-002', 'Reparación Conectividad', 'Cliente reporta intermitencia en la conexión de internet', 'EN_PROCESO', 'ALTA', 'REPARACION', 'Luis Fernando Torres', '+57 301 9876543', 'Calle 50 #30-15, Medellín, Antioquia', 2, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ORD-2025-003', 'Upgrade Fibra Óptica', 'Migración de ADSL a fibra óptica para cliente empresarial', 'ASIGNADA', 'CRITICA', 'UPGRADE', 'Empresa TecnoSoft SAS', '+57 4 2605050', 'Carrera 48 #26-85, Medellín, Antioquia', 3, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('ORD-2025-004', 'Mantenimiento Preventivo', 'Mantenimiento preventivo de equipos en oficina central', 'PAUSADA', 'BAJA', 'MANTENIMIENTO', 'TelcoNova Colombia', '+57 4 4441234', 'Carrera 65 #50-10, Medellín, Antioquia', 3, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('ORD-2025-005', 'Instalación WiFi Corporativo', 'Instalación de red WiFi empresarial con múltiples puntos de acceso', 'EN_PROCESO', 'ALTA', 'INSTALACION', 'Clínica San Rafael', '+57 4 3216549', 'Calle 52 #43-20, Medellín, Antioquia', 4, CURRENT_TIMESTAMP - INTERVAL '4 hours');

-- Insertar evidencias de prueba
INSERT INTO evidencias (orden_trabajo_id, tipo, contenido, creado_por) VALUES
(2, 'COMENTARIO', 'Iniciando diagnóstico de conectividad. Cliente reporta caídas frecuentes desde ayer.', 2),
(2, 'COMENTARIO', 'Detectado problema en el módem. Requiere reemplazo del equipo.', 2),
(5, 'COMENTARIO', 'Instalación del primer punto de acceso completada. Configurando SSID empresarial.', 4);

INSERT INTO materiales_utilizados (orden_trabajo_id,material_id,cantidad_utilizada, registrado_por,codigo_material,nombre_material,unidad_medida,precio_unitario) VALUES
(2, 5, 1, 2, 'MOD-DSL-001', 'Módem DSL', 'unidades', 45.50), -- Módem DSL para la reparación
(2, 1, 15, 2, 'CAB-UTP-001', 'Cable UTP Cat 6', 'metros', 2.50), -- Cable UTP para conexión
(5, 4, 3, 4, 'ROT-WIF-001', 'Router WiFi AC1200', 'unidades', 89.99), -- Routers WiFi para instalación corporativa
(5, 1, 50, 4, 'CAB-UTP-001', 'Cable UTP Cat 6', 'metros', 2.50); -- Cable UTP para cableado

-- =====================================================
-- COMENTARIOS DE DOCUMENTACIÓN
-- =====================================================

COMMENT ON SCHEMA telconova IS 'Esquema principal para TelcoNova SupportSuite';

COMMENT ON TABLE usuarios IS 'Tabla de usuarios del sistema (técnicos y administradores)';
COMMENT ON TABLE ordenes_trabajo IS 'Órdenes de trabajo asignadas a técnicos';
COMMENT ON TABLE evidencias IS 'Evidencias (comentarios y fotos) de las órdenes';
COMMENT ON TABLE materiales IS 'Catálogo de materiales disponibles';
COMMENT ON TABLE materiales_utilizados IS 'Registro de materiales consumidos por orden';
COMMENT ON TABLE historial_estados IS 'Auditoría de cambios de estado de órdenes';

-- =====================================================
-- VERIFICACIÓN DE INSTALACIÓN
-- =====================================================

-- Verificar que todas las tablas se crearon correctamente
SELECT
    schemaname,
    tablename,
    tableowner
FROM pg_tables
WHERE schemaname = 'telconova'
ORDER BY tablename;

-- Verificar constraints
SELECT
    conname,
    contype,
    conrelid::regclass
FROM pg_constraint
WHERE connamespace = 'telconova'::regnamespace
ORDER BY conrelid::regclass;

-- Mostrar estadísticas básicas
SELECT
    'usuarios' as tabla, COUNT(*) as registros FROM usuarios
UNION ALL
SELECT
    'ordenes_trabajo' as tabla, COUNT(*) as registros FROM ordenes_trabajo
UNION ALL
SELECT
    'materiales' as tabla, COUNT(*) as registros FROM materiales
UNION ALL
SELECT
    'evidencias' as tabla, COUNT(*) as registros FROM evidencias
UNION ALL
SELECT
    'materiales_utilizados' as tabla, COUNT(*) as registros FROM materiales_utilizados;