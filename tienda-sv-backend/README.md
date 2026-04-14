# 🇸🇻 Sistema de E-Commerce (El Salvador)

Este es un Backend robusto y escalable para una plataforma de E-Commerce configurada específicamente para las necesidades y regulaciones de El Salvador. 
Incorpora patrones de diseño modernos y está listo para integrarse con pasarelas de pago locales.

## 🏛 Arquitectura
El proyecto implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** para garantizar un dominio puro y fácilmente testeable:
- **`core/`**: Lógica de negocio (Dominio). Modelos de `Producto`, `Orden`, `Caja`. Aquí NO hay dependencias de frameworks externos.
- **`api/`**: Adaptadores de entrada. Controladores REST (`@RestController`).
- **`infrastructure/`**: Adaptadores de salida. Conexiones a bases de datos (JPA/PostgreSQL) y llamadas a APIs de pago (Pagadito, Wompi).

## 🚀 Tecnologías
- **Java 17** y **Spring Boot 3.x**
- **PostgreSQL** (Persistencia)
- **MapStruct** (Mapeo DTO <-> Entidad)
- **Spring Security + JWT** (Seguridad Autenticada y Roles: `ADMIN`, `CLIENTE`, `CAJERO`)
- **Docker & Docker Compose** (Despliegue)

## 🏦 Integraciones Locales
Preparado para integraciones (vía Ports):
- **Pagadito**: Pagos con tarjeta de crédito/débito.
- **Wompi SV**: Enlaces de pago, transferencias QR, Bitcoin (Chivo Wallet compatibility).

## 🛠️ Cómo ejecutar
1. Renombra `.env.example` a `.env` y configura tus variables.
2. Levanta la Base de Datos y la aplicación con Docker:
   ```bash
   docker-compose up -d
   ```
3. La aplicación estará corriendo en `http://localhost:8080`.

## 🛡️ Seguridad
Implementa un fuerte escrutinio en el mapeo de roles (RBAC). Asegúrate de validar correctamente los tokens JWT en los endpoints bancarizados y transaccionales.
