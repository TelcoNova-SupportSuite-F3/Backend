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
-- ADMINISTRADOR password: admin123
INSERT INTO usuarios (email, contrasena, nombre_completo, rol) VALUES
('admin@telconova.com', '$2a$12$kCr/ieDteyCjIDyHn2DrvOM3yWSvoSARGcNkUksm0pNJjzXvAGPyS', 'Administrador Sistema', 'ADMIN'),

-- TÉCNICOS (10) password: tecnico123
('juan.perez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Juan Carlos Pérez González', 'TECNICO'),
('maria.rodriguez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'María Fernanda Rodríguez López', 'TECNICO'),
('carlos.martinez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Carlos Andrés Martínez Silva', 'TECNICO'),
('ana.garcia@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Ana Lucía García Ruiz', 'TECNICO'),
('luis.hernandez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Luis Fernando Hernández Castro', 'TECNICO'),
('sofia.lopez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Sofía Alejandra López Moreno', 'TECNICO'),
('diego.torres@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Diego Alexander Torres Vega', 'TECNICO'),
('elena.jimenez@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Elena Patricia Jiménez Díaz', 'TECNICO'),
('andres.vargas@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Andrés Felipe Vargas Romero', 'TECNICO'),
('gabriela.morales@telconova.com', '$2a$12$dWMw2MSYZ1UV90eAQJHlPOoHBxEtMIIzptZjhdjEViRAlnls1Tpyy', 'Gabriela Isabel Morales Sánchez', 'TECNICO');

-- Insertar materiales de prueba
INSERT INTO materiales (codigo, nombre, descripcion, unidad_medida, precio_unitario, stock_disponible, activo) VALUES
-- CABLES Y CONECTORES (20)
('CAB-UTP-001', 'Cable UTP Cat 5e', 'Cable de red UTP Categoría 5e para redes locales', 'metros', 1.25, 5000, true),
('CAB-UTP-002', 'Cable UTP Cat 6', 'Cable de red UTP Categoría 6 para redes de alta velocidad', 'metros', 2.50, 3000, true),
('CAB-UTP-003', 'Cable UTP Cat 6A', 'Cable de red UTP Categoría 6A blindado', 'metros', 4.75, 1500, true),
('CAB-FIB-001', 'Cable Fibra Óptica Monomodo', 'Cable fibra óptica monomodo para larga distancia', 'metros', 8.90, 800, true),
('CAB-FIB-002', 'Cable Fibra Óptica Multimodo', 'Cable fibra óptica multimodo para corta distancia', 'metros', 6.50, 1200, true),
('CAB-COA-001', 'Cable Coaxial RG6', 'Cable coaxial RG6 para señales de TV y datos', 'metros', 3.20, 2000, true),
('CAB-COA-002', 'Cable Coaxial RG11', 'Cable coaxial RG11 para largas distancias', 'metros', 5.80, 800, true),
('CON-RJ45-001', 'Conector RJ45 Cat 5e', 'Conector RJ45 para cable UTP Cat 5e', 'unidades', 0.25, 10000, true),
('CON-RJ45-002', 'Conector RJ45 Cat 6', 'Conector RJ45 para cable UTP Cat 6', 'unidades', 0.35, 8000, true),
('CON-RJ45-003', 'Conector RJ45 Blindado', 'Conector RJ45 blindado para ambientes industriales', 'unidades', 0.85, 2000, true),
('CON-SC-001', 'Conector SC Fibra', 'Conector SC para fibra óptica', 'unidades', 2.50, 1000, true),
('CON-LC-001', 'Conector LC Fibra', 'Conector LC para fibra óptica', 'unidades', 3.20, 800, true),
('CON-F-001', 'Conector F Coaxial', 'Conector tipo F para cable coaxial', 'unidades', 0.45, 5000, true),
('CON-BNC-001', 'Conector BNC', 'Conector BNC para aplicaciones profesionales', 'unidades', 1.20, 1500, true),
('PLA-001', 'Placa de Pared RJ45', 'Placa de pared con 2 puertos RJ45', 'unidades', 4.50, 500, true),
('PLA-002', 'Placa de Pared Fibra', 'Placa de pared para fibra óptica', 'unidades', 8.90, 200, true),
('JAC-001', 'Jack RJ45 Cat 6', 'Jack RJ45 Categoría 6 para instalación', 'unidades', 2.80, 1000, true),
('PTC-001', 'Patch Cord Cat 6 - 1m', 'Cable patch cord Cat 6 de 1 metro', 'unidades', 3.50, 800, true),
('PTC-002', 'Patch Cord Cat 6 - 3m', 'Cable patch cord Cat 6 de 3 metros', 'unidades', 5.20, 600, true),
('PTC-003', 'Patch Cord Cat 6 - 5m', 'Cable patch cord Cat 6 de 5 metros', 'unidades', 7.80, 400, true),

-- EQUIPOS ACTIVOS (25)
('ROT-001', 'Router WiFi 6 AX1800', 'Router inalámbrico WiFi 6 de alta velocidad', 'unidades', 145.00, 50, true),
('ROT-002', 'Router Empresarial AC3200', 'Router empresarial de triple banda', 'unidades', 320.00, 25, true),
('ROT-003', 'Router Mesh WiFi 6', 'Sistema de router mesh para cobertura amplia', 'unidades', 280.00, 30, true),
('SWI-001', 'Switch 8 Puertos Gigabit', 'Switch no administrable de 8 puertos', 'unidades', 45.00, 100, true),
('SWI-002', 'Switch 16 Puertos Gigabit', 'Switch no administrable de 16 puertos', 'unidades', 85.00, 60, true),
('SWI-003', 'Switch 24 Puertos Managed', 'Switch administrable de 24 puertos', 'unidades', 180.00, 40, true),
('SWI-004', 'Switch PoE 8 Puertos', 'Switch PoE de 8 puertos para alimentar dispositivos', 'unidades', 120.00, 35, true),
('MOD-001', 'Módem ADSL2+', 'Módem ADSL2+ para conexiones de banda ancha', 'unidades', 55.00, 80, true),
('MOD-002', 'Módem Cable DOCSIS 3.0', 'Módem cable DOCSIS 3.0 de alta velocidad', 'unidades', 75.00, 65, true),
('MOD-003', 'Módem Fibra ONT', 'Terminal óptica de red para fibra', 'unidades', 95.00, 45, true),
('WAP-001', 'Punto Acceso WiFi 6', 'Punto de acceso inalámbrico WiFi 6', 'unidades', 85.00, 70, true),
('WAP-002', 'Punto Acceso Exterior', 'Punto de acceso para exteriores IP67', 'unidades', 150.00, 30, true),
('WAP-003', 'Punto Acceso Mesh', 'Punto de acceso mesh para redes malladas', 'unidades', 110.00, 40, true),
('REP-001', 'Repetidor WiFi AC1200', 'Repetidor WiFi de doble banda', 'unidades', 35.00, 120, true),
('REP-002', 'Extensor Powerline', 'Extensor de red por línea eléctrica', 'unidades', 65.00, 80, true),
('ANT-001', 'Antena Omnidireccional 2.4GHz', 'Antena omnidireccional de 9dBi', 'unidades', 25.00, 150, true),
('ANT-002', 'Antena Direccional 5GHz', 'Antena direccional de alta ganancia', 'unidades', 45.00, 100, true),
('ANT-003', 'Antena Panel 2.4/5GHz', 'Antena panel dual band', 'unidades', 35.00, 120, true),
('AMP-001', 'Amplificador de Señal', 'Amplificador de señal para cable coaxial', 'unidades', 28.00, 200, true),
('DIV-001', 'Divisor de Señal 2 Vías', 'Divisor de señal coaxial de 2 salidas', 'unidades', 8.50, 300, true),
('DIV-002', 'Divisor de Señal 4 Vías', 'Divisor de señal coaxial de 4 salidas', 'unidades', 12.00, 250, true),
('FIL-001', 'Filtro DSL', 'Filtro DSL para línea telefónica', 'unidades', 3.50, 500, true),
('BAL-001', 'Balun Video/Datos', 'Balun para transmisión de video y datos', 'unidades', 15.00, 180, true),
('CON-001', 'Conversor Fibra-Ethernet', 'Conversor de fibra óptica a Ethernet', 'unidades', 85.00, 60, true),
('INJ-001', 'Inyector PoE', 'Inyector Power over Ethernet', 'unidades', 22.00, 150, true),

-- HERRAMIENTAS Y ACCESORIOS (30)
('HER-001', 'Crimpeadora RJ45', 'Pinza crimpeadora para conectores RJ45', 'unidades', 25.00, 50, true),
('HER-002', 'Pelacables UTP', 'Pelacables para cable UTP', 'unidades', 8.50, 100, true),
('HER-003', 'Tester de Cable', 'Probador de cables de red', 'unidades', 35.00, 30, true),
('HER-004', 'Ponchadora 110', 'Herramienta ponchadora para jack 110', 'unidades', 45.00, 25, true),
('HER-005', 'Cortadora de Fibra', 'Cortadora de precisión para fibra óptica', 'unidades', 280.00, 10, true),
('HER-006', 'Fusionadora de Fibra', 'Fusionadora automática de fibra óptica', 'unidades', 1800.00, 5, true),
('HER-007', 'OTDR Medidor Fibra', 'Reflectómetro óptico para fibra', 'unidades', 3500.00, 3, true),
('HER-008', 'Medidor de Potencia', 'Medidor de potencia óptica', 'unidades', 180.00, 15, true),
('HER-009', 'Localizador Visual', 'Localizador visual de fallos en fibra', 'unidades', 45.00, 40, true),
('HER-010', 'Taladro Percutor', 'Taladro percutor para instalaciones', 'unidades', 120.00, 20, true),
('HER-011', 'Atornillador Eléctrico', 'Atornillador eléctrico inalámbrico', 'unidades', 65.00, 35, true),
('HER-012', 'Multímetro Digital', 'Multímetro digital profesional', 'unidades', 55.00, 25, true),
('HER-013', 'Escalera Telescópica', 'Escalera telescópica de aluminio', 'unidades', 180.00, 15, true),
('HER-014', 'Casco de Seguridad', 'Casco de seguridad industrial', 'unidades', 15.00, 100, true),
('HER-015', 'Guantes Dieléctricos', 'Guantes de seguridad dieléctricos', 'pares', 12.00, 80, true),
('ACC-001', 'Canaleta Plástica 25x16', 'Canaleta decorativa para cables', 'metros', 2.50, 1000, true),
('ACC-002', 'Canaleta Metálica 50x30', 'Canaleta metálica industrial', 'metros', 8.50, 500, true),
('ACC-003', 'Tubo Conduit PVC 3/4"', 'Tubo conduit de PVC de 3/4 pulgada', 'metros', 1.80, 2000, true),
('ACC-004', 'Tubo Conduit Metálico 1"', 'Tubo conduit metálico de 1 pulgada', 'metros', 4.50, 800, true),
('ACC-005', 'Abrazaderas Plásticas', 'Abrazaderas plásticas para cables', 'paquetes', 5.00, 300, true),
('ACC-006', 'Tornillos Autorroscantes', 'Tornillos para drywall y madera', 'cajas', 8.00, 200, true),
('ACC-007', 'Chazos Fischer', 'Chazos Fischer para concreto', 'cajas', 12.00, 150, true),
('ACC-008', 'Cinta Aislante', 'Cinta aislante eléctrica', 'rollos', 2.50, 500, true),
('ACC-009', 'Cinta Americana', 'Cinta americana multiuso', 'rollos', 4.50, 300, true),
('ACC-010', 'Silicona Sellante', 'Silicona sellante transparente', 'tubos', 3.50, 200, true),
('POW-001', 'UPS 650VA', 'Sistema de alimentación ininterrumpida', 'unidades', 85.00, 40, true),
('POW-002', 'UPS 1000VA', 'UPS de 1000VA para equipos críticos', 'unidades', 150.00, 25, true),
('POW-003', 'Fuente 12V 2A', 'Fuente de poder 12V 2A', 'unidades', 18.00, 100, true),
('POW-004', 'Fuente 24V 1A', 'Fuente de poder 24V 1A', 'unidades', 22.00, 80, true),
('POW-005', 'Regleta 6 Tomas', 'Regleta eléctrica de 6 tomas', 'unidades', 15.00, 120, true),

-- CAJAS Y GABINETES (15)
('CAJ-001', 'Caja de Distribución 12FO', 'Caja para distribución de fibra óptica', 'unidades', 35.00, 100, true),
('CAJ-002', 'Caja de Empalme Estanca', 'Caja de empalme a prueba de agua', 'unidades', 25.00, 150, true),
('CAJ-003', 'Caja Terminal Óptica', 'Caja terminal para fibra óptica', 'unidades', 45.00, 80, true),
('CAJ-004', 'Caja Derivación UTP', 'Caja de derivación para cable UTP', 'unidades', 8.50, 300, true),
('GAB-001', 'Gabinete Rack 12U', 'Gabinete rack de pared 12U', 'unidades', 180.00, 30, true),
('GAB-002', 'Gabinete Rack 6U', 'Gabinete rack de pared 6U', 'unidades', 120.00, 45, true),
('GAB-003', 'Gabinete Exterior IP65', 'Gabinete para exteriores IP65', 'unidades', 250.00, 20, true),
('PAN-001', 'Panel Patch 24P', 'Panel de parcheo de 24 puertos', 'unidades', 45.00, 60, true),
('PAN-002', 'Panel Patch 48P', 'Panel de parcheo de 48 puertos', 'unidades', 85.00, 35, true),
('PAN-003', 'Panel Fibra 12P', 'Panel para fibra óptica 12 puertos', 'unidades', 120.00, 25, true),
('BAN-001', 'Bandeja Rack 1U', 'Bandeja para rack de 1U', 'unidades', 25.00, 80, true),
('BAN-002', 'Bandeja Ventilada 2U', 'Bandeja ventilada de 2U', 'unidades', 45.00, 50, true),
('ORG-001', 'Organizador Cables 1U', 'Organizador de cables horizontal', 'unidades', 18.00, 100, true),
('ORG-002', 'Organizador Vertical', 'Organizador de cables vertical', 'unidades', 35.00, 60, true),
('TAP-001', 'Tapa Ciega 1U', 'Tapa ciega para rack 1U', 'unidades', 8.00, 200, true),

-- CONSUMIBLES (10)
('CON-SOL-001', 'Soldadura Sin Plomo', 'Soldadura sin plomo para electrónicos', 'rollos', 15.00, 50, true),
('CON-FLU-001', 'Flux para Soldadura', 'Pasta flux para soldadura', 'frascos', 8.00, 80, true),
('CON-DES-001', 'Desoxidante Contactos', 'Spray desoxidante para contactos', 'frascos', 12.00, 100, true),
('CON-LIM-001', 'Limpiador Isopropílico', 'Alcohol isopropílico para limpieza', 'frascos', 6.00, 150, true),
('CON-GRA-001', 'Grasa Dieléctrica', 'Grasa dieléctrica para conectores', 'tubos', 4.50, 200, true),
('CON-VEL-001', 'Velcro Adhesivo', 'Cinta velcro para organización', 'metros', 2.00, 500, true),
('CON-ESP-001', 'Espuma Expansiva', 'Espuma expansiva para sellado', 'latas', 8.50, 120, true),
('CON-MAR-001', 'Marcadores Permanentes', 'Marcadores para identificación', 'sets', 5.00, 100, true),
('CON-ETI-001', 'Etiquetas Adhesivas', 'Etiquetas para identificación', 'rollos', 3.50, 300, true),
('CON-PRO-001', 'Protectores RJ45', 'Protectores de polvo para RJ45', 'paquetes', 2.50, 400, true);

-- =====================================================
-- ÓRDENES DE TRABAJO: 30 ÓRDENES (5 EN_PROCESO, 25 ASIGNADA)
-- =====================================================

-- IDs de técnicos: 2-11
-- Prioridades: MEDIA, ALTA, CRITICA

INSERT INTO ordenes_trabajo (numero_orden, titulo, descripcion, estado, prioridad, tipo_servicio, cliente_nombre, cliente_telefono, direccion, tecnico_asignado_id, fecha_asignacion,fecha_inicio_trabajo, fecha_creacion) VALUES

-- 5 ÓRDENES EN_PROCESO
('ORD-2025-001', 'Instalación Fibra Óptica Residencial', 'Instalación de fibra óptica 200Mbps para cliente residencial en Laureles', 'EN_PROCESO', 'ALTA', 'INSTALACION', 'Kevin Brand', '+573206875298', 'Carrera 75 #45-89, Laureles, Medellín', 2, CURRENT_TIMESTAMP - INTERVAL '12 hours',CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ORD-2025-002', 'Reparación Conectividad Empresarial', 'Reparación de conectividad de internet empresarial - falla intermitente', 'EN_PROCESO', 'CRITICA', 'REPARACION', 'Empresa Logística Express SAS', '+573044445566', 'Calle 30 Sur #48-25, Envigado', 3, CURRENT_TIMESTAMP - INTERVAL '12 hours',CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('ORD-2025-003', 'Upgrade Red WiFi Corporativa', 'Actualización de red WiFi corporativa a WiFi 6 en edificio de oficinas', 'EN_PROCESO', 'MEDIA', 'UPGRADE', 'Consultores Estratégicos Ltda', '+573143507788', 'Carrera 43A #16-30, El Poblado, Medellín', 4, CURRENT_TIMESTAMP - INTERVAL '14 hours', CURRENT_TIMESTAMP - INTERVAL '1 hours',CURRENT_TIMESTAMP - INTERVAL '2 days'),
('ORD-2025-004', 'Mantenimiento Preventivo Torres', 'Mantenimiento preventivo en torre de telecomunicaciones sector norte', 'EN_PROCESO', 'MEDIA', 'MANTENIMIENTO', 'TelcoNova Colombia SAS', '+571344441234', 'Calle 67 #52-20, Bello, Antioquia', 5, CURRENT_TIMESTAMP - INTERVAL '21 hour', CURRENT_TIMESTAMP - INTERVAL '2 hours',CURRENT_TIMESTAMP - INTERVAL '3 days'),
('ORD-2025-005', 'Instalación Red Punto Venta', 'Instalación de red para nuevos puntos de venta en centro comercial', 'EN_PROCESO', 'ALTA', 'INSTALACION', 'Tiendas D1 Colombia', '+573145002233', 'Carrera 65 #33-10, Centro, Medellín', 6, CURRENT_TIMESTAMP - INTERVAL '25 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours',CURRENT_TIMESTAMP - INTERVAL '2 days'),

-- 25 ÓRDENES ASIGNADAS
('ORD-2025-006', 'Instalación Internet Hogar', 'Instalación de internet residencial 100Mbps', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Jefferson Andrés Espejo Góez', '+573046333799', 'Carrera 80 #32-45, Robledo, Medellín', 2, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('ORD-2025-007', 'Reparación Módem Cliente', 'Reparación de módem ADSL cliente residencial', 'ASIGNADA', 'MEDIA', 'REPARACION', 'Darwin Andrés Tangarife Avendaño', '+573015781171', 'Calle 45 #70-12, Belén, Medellín', 3, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('ORD-2025-008', 'Instalación WiFi Empresa', 'Instalación de red WiFi empresarial en oficina nueva', 'ASIGNADA', 'ALTA', 'INSTALACION', 'Johana Liseth Sevillano Herrera', '+573196960915', 'Carrera 48 #26-85, La Candelaria, Medellín', 4, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('ORD-2025-009', 'Mantenimiento Switch Central', 'Mantenimiento correctivo de switch en central telefónica', 'ASIGNADA', 'CRITICA', 'MANTENIMIENTO', 'Central Telefónica Norte', '+573145287825', 'Carrera 52 #67-30, Aranjuez, Medellín', 5, CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes'),
('ORD-2025-010', 'Upgrade Fibra Óptica', 'Migración de ADSL a fibra óptica cliente empresarial', 'ASIGNADA', 'ALTA', 'UPGRADE', 'Consultoría Jurídica ABC', '+573142562324', 'Calle 10A Sur #43-50, El Poblado, Medellín', 6, CURRENT_TIMESTAMP - INTERVAL '20 minutes', CURRENT_TIMESTAMP - INTERVAL '40 minutes'),
('ORD-2025-011', 'Instalación CCTV IP', 'Instalación de sistema de cámaras IP sobre red existente', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Supermercado La Economía', '+573012506677', 'Carrera 65 #48-20, Buenos Aires, Medellín', 7, CURRENT_TIMESTAMP - INTERVAL '1 hour 15 minutes', CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes'),
('ORD-2025-012', 'Reparación Antena WiFi', 'Reparación de antena WiFi exterior dañada por tormenta', 'ASIGNADA', 'ALTA', 'REPARACION', 'Finca Villa Hermosa', '+573005554433', 'Vereda La Madera, La Estrella, Antioquia', 8, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('ORD-2025-013', 'Instalación Red Colegio', 'Instalación de red de datos en aulas de sistemas', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Colegio San José de Las Vegas', '+573145389900', 'Calle 25 Sur #55-30, Las Vegas, Medellín', 9, CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes', CURRENT_TIMESTAMP - INTERVAL '3 hours 45 minutes'),
('ORD-2025-014', 'Mantenimiento Rack Datos', 'Mantenimiento preventivo de rack de datos en oficina principal', 'ASIGNADA', 'MEDIA', 'MANTENIMIENTO', 'Constructora Andina Ltda', '+573144442211', 'Carrera 50 #60-40, Centro, Medellín', 10, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
('ORD-2025-015', 'Reparación Fibra Cortada', 'Reparación de fibra óptica cortada por obras civiles', 'ASIGNADA', 'CRITICA', 'REPARACION', 'Conjunto Residencial Torres del Sol', '+573144448877', 'Carrera 70 #80-50, Robledo, Medellín', 11, CURRENT_TIMESTAMP - INTERVAL '1 hour 45 minutes', CURRENT_TIMESTAMP - INTERVAL '2 hours 15 minutes'),
('ORD-2025-016', 'Instalación Internet Café', 'Instalación de conexión de alta velocidad para café internet', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Café Internet Digital', '+573017778899', 'Calle 33 #65-12, Doce de Octubre, Medellín', 2, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('ORD-2025-017', 'Upgrade Router Empresarial', 'Actualización de router empresarial por obsolescencia', 'ASIGNADA', 'ALTA', 'UPGRADE', 'Laboratorio Clínico Salud Total', '+573143505566', 'Carrera 43A #5A-113, El Poblado, Medellín', 3, CURRENT_TIMESTAMP - INTERVAL '3 hours 20 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours 30 minutes'),
('ORD-2025-018', 'Instalación VPN Empresarial', 'Configuración de VPN para trabajo remoto', 'ASIGNADA', 'ALTA', 'INSTALACION', 'Agencia de Viajes Mundo Tour', '+573142503344', 'Calle 70 #52-20, Manrique, Medellín', 4, CURRENT_TIMESTAMP - INTERVAL '5 hours 30 minutes', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
('ORD-2025-019', 'Reparación Switch PoE', 'Reparación de switch PoE que no alimenta cámaras IP', 'ASIGNADA', 'MEDIA', 'REPARACION', 'Centro Comercial Plaza Mayor', '+573144449955', 'Carrera 65 #50-30, Candelaria, Medellín', 5, CURRENT_TIMESTAMP - INTERVAL '2 hours 45 minutes', CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes'),
('ORD-2025-020', 'Instalación Fibra Edificio', 'Cableado estructurado con fibra óptica en edificio nuevo', 'ASIGNADA', 'CRITICA', 'INSTALACION', 'Edificio Empresarial Sky Tower', '+573144441122', 'Carrera 43A #14-50, El Poblado, Medellín', 6, CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes', CURRENT_TIMESTAMP - INTERVAL '2 hours 45 minutes'),
('ORD-2025-021', 'Mantenimiento Antenas', 'Mantenimiento preventivo de antenas en azotea', 'ASIGNADA', 'MEDIA', 'MANTENIMIENTO', 'Hospital General del Norte', '+573144446677', 'Calle 67 #52-44, Bello, Antioquia', 7, CURRENT_TIMESTAMP - INTERVAL '4 hours 15 minutes', CURRENT_TIMESTAMP - INTERVAL '5 hours 30 minutes'),
('ORD-2025-022', 'Reparación Conexión ADSL', 'Reparación de línea ADSL con baja velocidad', 'ASIGNADA', 'MEDIA', 'REPARACION', 'Panadería y Repostería El Horno', '+573004445566', 'Carrera 45 #32-10, Villa Hermosa, Medellín', 8, CURRENT_TIMESTAMP - INTERVAL '6 hours', CURRENT_TIMESTAMP - INTERVAL '7 hours'),
('ORD-2025-023', 'Instalación Red Restaurante', 'Instalación de red WiFi y cableado para sistema POS', 'ASIGNADA', 'ALTA', 'INSTALACION', 'Restaurante La Hacienda', '+573143507799', 'Calle 10 Sur #43A-50, El Poblado, Medellín', 9, CURRENT_TIMESTAMP - INTERVAL '3 hours 45 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours 15 minutes'),
('ORD-2025-024', 'Upgrade Central Telefónica', 'Actualización de central telefónica IP', 'ASIGNADA', 'CRITICA', 'UPGRADE', 'Clínica Odontológica Sonrisas', '+573142608899', 'Carrera 70 #45-30, Laureles, Medellín', 10, CURRENT_TIMESTAMP - INTERVAL '2 hours 20 minutes', CURRENT_TIMESTAMP - INTERVAL '3 hours 10 minutes'),
('ORD-2025-025', 'Instalación WiFi Hotel', 'Instalación de red WiFi de alta capacidad en hotel', 'ASIGNADA', 'ALTA', 'INSTALACION', 'Hotel Business Plaza', '+573144443366', 'Calle 50 #70-200, Estadio, Medellín', 11, CURRENT_TIMESTAMP - INTERVAL '5 hours 45 minutes', CURRENT_TIMESTAMP - INTERVAL '6 hours 30 minutes'),
('ORD-2025-026', 'Reparación Fibra Domiciliaria', 'Reparación de acometida de fibra óptica residencial', 'ASIGNADA', 'MEDIA', 'REPARACION', 'Alejandro Restrepo Cardona', '+573048887766', 'Carrera 80 #50-20, Castilla, Medellín', 2, CURRENT_TIMESTAMP - INTERVAL '1 hour 10 minutes', CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes'),
('ORD-2025-027', 'Instalación Cámaras IP', 'Instalación de sistema de videovigilancia IP', 'ASIGNADA', 'MEDIA', 'INSTALACION', 'Farmacia Cruz Verde Sucursal 45', '+573144442255', 'Carrera 65 #35-40, Buenos Aires, Medellín', 3, CURRENT_TIMESTAMP - INTERVAL '7 hours', CURRENT_TIMESTAMP - INTERVAL '8 hours'),
('ORD-2025-028', 'Mantenimiento UPS Sala Servidores', 'Mantenimiento correctivo de UPS en sala de servidores', 'ASIGNADA', 'ALTA', 'MANTENIMIENTO', 'Banco Popular Sucursal Norte', '+573144445599', 'Calle 75 #65-30, Aranjuez, Medellín', 4, CURRENT_TIMESTAMP - INTERVAL '4 hours 30 minutes', CURRENT_TIMESTAMP - INTERVAL '5 hours 45 minutes'),
('ORD-2025-029', 'Reparación Router WiFi', 'Reparación de router WiFi empresarial con fallas de conectividad', 'ASIGNADA', 'MEDIA', 'REPARACION', 'Oficina Contable Números & Cia', '+573143504477', 'Carrera 43A #20-50, El Poblado, Medellín', 5, CURRENT_TIMESTAMP - INTERVAL '3 hours 15 minutes', CURRENT_TIMESTAMP - INTERVAL '4 hours 5 minutes'),
('ORD-2025-030', 'Instalación Red Laboratorio', 'Instalación de red de datos en laboratorio de análisis clínicos', 'ASIGNADA', 'CRITICA', 'INSTALACION', 'Laboratorio Médico Diagnósticos', '+573142607788', 'Calle 67 #48-30, Manrique, Medellín', 6, CURRENT_TIMESTAMP - INTERVAL '2 hours 10 minutes', CURRENT_TIMESTAMP - INTERVAL '2 hours 55 minutes');

-- =====================================================
-- EVIDENCIAS: 10 COMENTARIOS PARA ÓRDENES EN_PROCESO
-- =====================================================

-- Solo para órdenes con ID 1-5 (que están EN_PROCESO)
INSERT INTO evidencias (orden_trabajo_id, tipo, contenido, fecha_creacion, creado_por) VALUES

-- Evidencias para ORD-2025-001 (ID: 1) - Juan Pérez
('1', 'COMENTARIO', 'Iniciando instalación de fibra óptica. Cliente muy colaborativo, facilita acceso a todas las áreas necesarias.', CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes', 2),
('1', 'COMENTARIO', 'Tendido de fibra óptica completado desde poste hasta caja de distribución. Realizando empalmes en caja terminal.', CURRENT_TIMESTAMP - INTERVAL '1 hour 45 minutes', 2),

-- Evidencias para ORD-2025-002 (ID: 2) - María Rodríguez
('2', 'COMENTARIO', 'Llegué al sitio. Confirmado problema de conectividad intermitente. Iniciando diagnóstico de equipos.', CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes', 3),
('2', 'COMENTARIO', 'Problema identificado: switch principal presenta fallas. Reemplazando equipo por uno nuevo del inventario.', CURRENT_TIMESTAMP - INTERVAL '45 minutes', 3),

-- Evidencias para ORD-2025-003 (ID: 3) - Carlos Martínez
('3', 'COMENTARIO', 'Iniciando upgrade de WiFi corporativo. Relevamiento completo de ubicaciones actuales de puntos de acceso.', CURRENT_TIMESTAMP - INTERVAL '3 hours 15 minutes', 4),
('3', 'COMENTARIO', 'Instalación de nuevos access points WiFi 6 en progreso. Configurando red mesh para mejor cobertura.', CURRENT_TIMESTAMP - INTERVAL '2 hours', 4),

-- Evidencias para ORD-2025-004 (ID: 4) - Ana García
('4', 'COMENTARIO', 'Mantenimiento preventivo iniciado. Revisión de equipos en torre, todo en orden. Limpieza de conectores.', CURRENT_TIMESTAMP - INTERVAL '45 minutes', 5),
('4', 'COMENTARIO', 'Calibración de antenas completada. Niveles de señal óptimos. Aplicando protección anticorrosiva.', CURRENT_TIMESTAMP - INTERVAL '20 minutes', 5),

-- Evidencias para ORD-2025-005 (ID: 5) - Luis Hernández
('5', 'COMENTARIO', 'Instalación de red para puntos de venta en progreso. Cableado estructurado en 8 locales comerciales.', CURRENT_TIMESTAMP - INTERVAL '4 hours 30 minutes', 6),
('5', 'COMENTARIO', 'Configuración de switches y puntos de acceso completada. Realizando pruebas de conectividad en cada POS.', CURRENT_TIMESTAMP - INTERVAL '2 hours 15 minutes', 6);

-- =====================================================
-- MATERIALES UTILIZADOS: SOLO PARA ÓRDENES EN_PROCESO
-- =====================================================

-- Materiales para ORD-2025-001 (Instalación Fibra Óptica) - Juan Pérez
INSERT INTO materiales_utilizados
(orden_trabajo_id, material_id, cantidad_utilizada, registrado_por, codigo_material, nombre_material, unidad_medida, precio_unitario, fecha_registro)
VALUES
-- Materiales para ORD-2025-001 (Instalación Fibra Óptica) - Juan Pérez
(1, 4, 150, 2, 'CAB-FIB-001', 'Cable Fibra Óptica Monomodo', 'metros', 8.90, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(1, 11, 2, 2, 'CON-SC-001', 'Conector SC Fibra', 'unidades', 2.50, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(1, 33, 1, 2, 'CAJ-003', 'Caja Terminal Óptica', 'unidades', 45.00, CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes'),
(1, 81, 1, 2, 'CAJ-001', 'Caja de Distribución 12FO', 'unidades', 35.00, CURRENT_TIMESTAMP - INTERVAL '1 hour'),

-- Materiales para ORD-2025-002 (Reparación Conectividad) - María Rodríguez
(2, 24, 1, 3, 'SWI-003', 'Switch 24 Puertos Managed', 'unidades', 180.00, CURRENT_TIMESTAMP - INTERVAL '1 hour 15 minutes'),
(2, 18, 4, 3, 'PTC-001', 'Patch Cord Cat 6 - 1m', 'unidades', 3.50, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 19, 2, 3, 'PTC-002', 'Patch Cord Cat 6 - 3m', 'unidades', 5.20, CURRENT_TIMESTAMP - INTERVAL '45 minutes'),
(2, 63, 1, 3, 'POW-001', 'UPS 650VA', 'unidades', 85.00, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),

-- Materiales para ORD-2025-003 (Upgrade WiFi) - Carlos Martínez
(3, 31, 8, 4, 'WAP-001', 'Punto Acceso WiFi 6', 'unidades', 85.00, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(3, 33, 4, 4, 'WAP-003', 'Punto Acceso Mesh', 'unidades', 110.00, CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes'),
(3, 25, 2, 4, 'INJ-001', 'Inyector PoE', 'unidades', 22.00, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(3, 2, 200, 4, 'CAB-UTP-002', 'Cable UTP Cat 6', 'metros', 2.50, CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes'),

-- Materiales para ORD-2025-004 (Mantenimiento Torres) - Ana García
(4, 36, 2, 5, 'ANT-001', 'Antena Omnidireccional 2.4GHz', 'unidades', 25.00, CURRENT_TIMESTAMP - INTERVAL '40 minutes'),
(4, 73, 5, 5, 'CON-DES-001', 'Desoxidante Contactos', 'frascos', 12.00, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
(4, 74, 3, 5, 'CON-LIM-001', 'Limpiador Isopropílico', 'frascos', 6.00, CURRENT_TIMESTAMP - INTERVAL '25 minutes'),
(4, 75, 2, 5, 'CON-GRA-001', 'Grasa Dieléctrica', 'tubos', 4.50, CURRENT_TIMESTAMP - INTERVAL '15 minutes'),

-- Materiales para ORD-2025-005 (Red Puntos Venta) - Luis Hernández
(5, 22, 3, 6, 'SWI-001', 'Switch 8 Puertos Gigabit', 'unidades', 45.00, CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(5, 21, 8, 6, 'ROT-001', 'Router WiFi 6 AX1800', 'unidades', 145.00, CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes'),
(5, 2, 500, 6, 'CAB-UTP-002', 'Cable UTP Cat 6', 'metros', 2.50, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(5, 9, 50, 6, 'CON-RJ45-002', 'Conector RJ45 Cat 6', 'unidades', 0.35, CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes'),
(5, 15, 16, 6, 'PLA-001', 'Placa de Pared RJ45', 'unidades', 4.50, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(5, 61, 20, 6, 'ACC-001', 'Canaleta Plástica 25x16', 'metros', 2.50, CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes');


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