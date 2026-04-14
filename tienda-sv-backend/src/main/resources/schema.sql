-- SCRIPT DE BASE DE DATOS (DIALECTO: MYSQL / LARAGON)
-- Automáticamente inyectado por Spring Boot al arrancar por primera vez

CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    estado BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- =====================================================================
-- DATOS SEMILLA: Roles del sistema
-- =====================================================================
INSERT IGNORE INTO roles (nombre, descripcion) VALUES
    ('ADMIN',   'Administrador con acceso total al sistema'),
    ('CAJERO',  'Operador de caja, ventas y movimientos de dinero'),
    ('CLIENTE', 'Cliente registrado de la tienda en línea');

-- =====================================================================
-- DATOS SEMILLA: Usuarios por defecto
-- Contraseñas en BCrypt (cost=10):
--   admin123   -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--   cajero123  -> $2a$10$d2h3Z1LpBWNnq5RXyf2G1.9UZT.3bKWt.3dJpQl2sKYAfS6LXdfa2
--   cliente123 -> $2a$10$TKh8H1.PzlGW4/v.LDVxjeSn1xbKIFBjAO7x5G8r2Y8b7Z9nG9nxi
-- =====================================================================
INSERT IGNORE INTO usuarios (username, email, password_hash, role_id) VALUES
    ('admin',
     'admin@tiendasv.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     (SELECT role_id FROM roles WHERE nombre = 'ADMIN')),

    ('cajero',
     'cajero@tiendasv.com',
     '$2a$10$d2h3Z1LpBWNnq5RXyf2G1.9UZT.3bKWt.3dJpQl2sKYAfS6LXdfa2',
     (SELECT role_id FROM roles WHERE nombre = 'CAJERO')),

    ('cliente',
     'cliente@tiendasv.com',
     '$2a$10$TKh8H1.PzlGW4/v.LDVxjeSn1xbKIFBjAO7x5G8r2Y8b7Z9nG9nxi',
     (SELECT role_id FROM roles WHERE nombre = 'CLIENTE'));

CREATE TABLE IF NOT EXISTS sesiones_caja (
    sesion_id INT AUTO_INCREMENT PRIMARY KEY,
    cajero_id INT NOT NULL,
    fecha_apertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP NULL,
    monto_inicial DECIMAL(12,2) NOT NULL CHECK (monto_inicial >= 0),
    monto_final DECIMAL(12,2) NULL,
    estado CHAR(1) DEFAULT 'A' CHECK (estado IN ('A', 'C')),
    FOREIGN KEY (cajero_id) REFERENCES usuarios(usuario_id)
);

CREATE TABLE IF NOT EXISTS movimientos_caja (
    movimiento_id INT AUTO_INCREMENT PRIMARY KEY,
    sesion_id INT NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA')),
    metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO',
    monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    descripcion VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sesion_id) REFERENCES sesiones_caja(sesion_id)
);

CREATE TABLE IF NOT EXISTS productos (
    producto_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock INT DEFAULT 0 CHECK (stock >= 0),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS ventas (
    venta_id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    sesion_id INT,
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    codigo_transaccion VARCHAR(200),
    total DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    estado_venta VARCHAR(20) DEFAULT 'COMPLETADA' CHECK (estado_venta IN ('PENDIENTE', 'COMPLETADA', 'ANULADA')),
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    FOREIGN KEY (sesion_id) REFERENCES sesiones_caja(sesion_id)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    detalle_id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(12,2) AS (cantidad * precio_unitario) STORED,
    FOREIGN KEY (venta_id) REFERENCES ventas(venta_id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(producto_id)
);
