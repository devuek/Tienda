-- =========================================================================
-- SCRIPT DE BASE DE DATOS MEJORADO (DIALECTO: POSTGRESQL)
-- Optimizado para el Backend Spring Boot y adaptado para El Salvador
-- =========================================================================

-- 1. CREACIÓN DE TABLAS DE SEGURIDAD Y ROLES
CREATE TABLE roles (
    role_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios (
    usuario_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE, -- QA: Añadido para tickets y login digital
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    estado BOOLEAN DEFAULT TRUE, -- Migrado de BIT a BOOLEAN para PostgreSQL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- 2. GESTIÓN DE CAJA FISICA Y DIGITAL (ENTRADAS/SALIDAS)
CREATE TABLE sesiones_caja (
    sesion_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cajero_id INT NOT NULL,
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP NULL,
    monto_inicial DECIMAL(12,2) NOT NULL CHECK (monto_inicial >= 0),
    monto_final DECIMAL(12,2) NULL,
    estado CHAR(1) DEFAULT 'A' CHECK (estado IN ('A', 'C')), -- 'A' Abierta, 'C' Cerrada
    FOREIGN KEY (cajero_id) REFERENCES usuarios(usuario_id)
);

CREATE TABLE movimientos_caja (
    movimiento_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sesion_id INT NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA')),
    metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO', -- Efectivo, Wompi, Pagadito, Bitcoin
    monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    descripcion VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sesion_id) REFERENCES sesiones_caja(sesion_id)
);

-- 3. VENTAS Y PRODUCTOS
CREATE TABLE productos (
    producto_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock INT DEFAULT 0 CHECK (stock >= 0), -- QA ZERO-BUGS: Impide a nivel Motor DB que se venda sin stock.
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE ventas (
    venta_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id INT NOT NULL, -- Comprador (Cliente online o Cliente Mostrador)
    sesion_id INT,  -- Puede ser NULL si es venta E-Commerce pura (no pasa por caja física)
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO', -- Crucial para integraciones locales
    codigo_transaccion VARCHAR(200), -- Para guardar la referencia del banco/Wompi
    total DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    estado_venta VARCHAR(20) DEFAULT 'COMPLETADA' CHECK (estado_venta IN ('PENDIENTE', 'COMPLETADA', 'ANULADA')),
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    FOREIGN KEY (sesion_id) REFERENCES sesiones_caja(sesion_id)
);

-- 🚨 LÓGICA DE NEGOCIO CRÍTICA (Faltante en tu diseño original)
CREATE TABLE detalle_ventas (
    detalle_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    -- BIG DATA / EFICIENCIA: Generación estandarizada en motor de la línea de facturación
    subtotal DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED, 
    FOREIGN KEY (venta_id) REFERENCES ventas(venta_id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(producto_id)
);

-- 4. ÍNDICES DE RENDIMIENTO EXTREMO
CREATE INDEX ix_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX ix_sesiones_estado ON sesiones_caja(estado, cajero_id);
CREATE INDEX ix_usuarios_username ON usuarios(username);
CREATE INDEX ix_productos_activo ON productos(activo, stock);
