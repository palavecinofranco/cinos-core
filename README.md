# CINOS Core API

**Backend del marketplace automotor CINOS** — Una plataforma completa para comprar, vender y conectar con entusiastas del mundo automotor.

---

## Tabla de Contenidos

- [Acerca del Proyecto](#acerca-del-proyecto)
- [Tech Stack](#tech-stack)
- [Arquitectura](#arquitectura)
- [Funcionalidades](#funcionalidades)
- [Requisitos Previos](#requisitos-previos)
- [Instalacion](#instalacion)
- [Variables de Entorno](#variables-de-entorno)
- [Ejecucion](#ejecucion)
- [Docker](#docker)
- [API Endpoints](#api-endpoints)
- [WebSockets](#websockets)
- [Base de Datos](#base-de-datos)

---

## Acerca del Proyecto

CINOS Core es la API REST que impulsa el marketplace automotor CINOS. Permite a los usuarios publicar vehiculos, gestionar suscripciones premium, verificar tecnicamente sus autos, intercambiar mensajes en tiempo real y mucho mas.

---

## Tech Stack

| Capa | Tecnologia |
|------|-----------|
| **Framework** | Spring Boot 3.3.3 |
| **Lenguaje** | Java 17 |
| **Build** | Maven 3.9.5 |
| **Base de Datos** | MySQL 8.0+ |
| **ORM** | Spring Data JPA / Hibernate |
| **Seguridad** | Spring Security 6 + JWT (jjwt 0.12.6) |
| **Pagos** | Stripe 24.10.0 |
| **Storage** | Google Cloud Storage |
| **Notificaciones** | Firebase Cloud Messaging |
| **Mensajeria** | WebSocket (STOMP + SockJS) |
| **Email** | Spring Mail + Thymeleaf |
| **Contenedores** | Docker (multi-stage build) |
| **Mapping** | MapStruct 1.6.3 |
| **Imagenes** | Thumbnailator 0.4.20 |

---

## Arquitectura

```
src/main/java/org/cinos/core/
|
|-- auth/                 # Autenticacion JWT + Google OAuth
|-- users/                # Gestion de usuarios y cuentas
|-- posts/                # Publicaciones de vehiculos
|-- messages/             # Mensajeria en tiempo real (WebSocket)
|-- notifications/        # Push notifications (Firebase)
|-- stripe/               # Pagos y suscripciones
|-- technical_verification/  # Verificacion tecnica vehicular
|-- follows/              # Sistema de seguimiento social
|-- search/               # Busqueda global
|-- mail/                 # Servicio de emails
|-- config/               # Configuraciones globales (CORS, GCS, Firebase)
|-- health/               # Health check
`-- utils/                # Excepciones y utilidades
```

El proyecto utiliza **Spring Modulith** para mantener una arquitectura modular y desacoplada.

---

## Funcionalidades

### Autenticacion y Seguridad
- Registro e inicio de sesion con JWT (access token 60min + refresh token 30 dias)
- Login con Google OAuth 2.0
- Control de acceso basado en roles: `USER`, `ADMIN`, `PREMIUM`

### Marketplace de Vehiculos
- Publicacion de vehiculos con multiples imagenes
- Filtrado avanzado por marca, modelo, anio, precio, kilometraje, combustible, transmision
- Feed personalizado y recomendaciones basadas en preferencias
- Sistema de comentarios y guardado de publicaciones

### Verificacion Tecnica
- Flujo de verificacion en multiples pasos (orden, aceptacion, proceso, resultado)
- Reportes detallados con porcentajes por componente
- Sistema de creditos para usuarios premium

### Mensajeria en Tiempo Real
- Chat entre usuarios via WebSocket (STOMP + SockJS)
- Gestion de conversaciones
- Estado de mensajes (enviado/visto)

### Suscripciones Premium
- Integracion completa con Stripe
- Checkout sessions y webhooks
- Creditos mensuales de verificacion tecnica
- Compra de acceso a reportes individuales

### Notificaciones Push
- Firebase Cloud Messaging
- Notificaciones automaticas por nuevos posts que coincidan con preferencias
- Alertas de suscripcion y mensajes

### Procesamiento de Imagenes
- Generacion automatica de thumbnails en multiples resoluciones
- Almacenamiento en Google Cloud Storage
- Soporte para JPG, PNG, GIF, WebP (hasta 100MB)

### Social
- Seguir/dejar de seguir usuarios
- Busqueda global de usuarios y publicaciones

---

## Requisitos Previos

- **Java** 17+
- **Maven** 3.9.5+
- **MySQL** 8.0+
- **Git**

---

## Instalacion

```bash
# Clonar el repositorio
git clone <url-del-repositorio>
cd core

# Instalar dependencias y compilar
mvn clean package -DskipTests
```

---

## Variables de Entorno

Configurar las siguientes variables antes de ejecutar la aplicacion:

```env
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cinos_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tu_password

# JWT
JWT_SECRET_KEY=<clave-secreta-base64>

# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Google Cloud Storage
GCP_CREDENTIALS_BASE64=<credenciales-gcs-base64>

# Firebase
FIREBASE_CREDENTIALS_BASE64=<credenciales-firebase-base64>

# Email
EMAIL_USERNAME=tu-email@cinos.org
EMAIL_PASSWORD=tu-app-password
```

---

## Ejecucion

```bash
# Opcion 1: Con Maven
mvn spring-boot:run

# Opcion 2: Ejecutar el JAR
java -jar target/cinos-core-1.7.0-SNAPSHOT.jar
```

La API estara disponible en `http://localhost:8080`

Verificar con: `GET http://localhost:8080/health`

---

## Docker

```bash
# Construir la imagen
docker build -t cinos-core:latest .

# Ejecutar el contenedor
docker run -d \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/cinos_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET_KEY=tu-clave-secreta \
  -e GCP_CREDENTIALS_BASE64=credenciales-gcs \
  -e FIREBASE_CREDENTIALS_BASE64=credenciales-firebase \
  -e STRIPE_SECRET_KEY=sk_test_xxx \
  -p 8080:8080 \
  cinos-core:latest
```

---

## API Endpoints

### Autenticacion

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar usuario |
| `POST` | `/auth/login` | Iniciar sesion |
| `POST` | `/auth/google` | Login con Google |
| `GET` | `/auth/refresh` | Refrescar token |

### Usuarios

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/user/logged` | Perfil del usuario logueado |
| `GET` | `/user/account/logged` | Cuenta del usuario logueado |
| `PUT` | `/user/account/update` | Actualizar cuenta (multipart) |
| `PATCH` | `/user/recommendations-preferences` | Actualizar preferencias |
| `GET` | `/user/premium/stats` | Estadisticas premium |

### Publicaciones

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/post/pageable` | Listar posts paginados |
| `POST` | `/post/filter` | Filtrar posts |
| `GET` | `/post/feed/{userId}` | Feed del usuario |
| `GET` | `/post/{id}` | Detalle de publicacion |
| `POST` | `/post/create` | Crear publicacion (multipart) |
| `POST` | `/post/comment` | Comentar publicacion |
| `POST` | `/post/deactivate/{id}` | Desactivar publicacion |

### Mensajes

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `POST` | `/messages/send` | Enviar mensaje |
| `GET` | `/messages/conversations/{userId}` | Conversaciones del usuario |
| `GET` | `/messages/conversation/messages/{id}` | Mensajes de conversacion |

### Suscripciones

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/subscriptions/plans` | Planes disponibles |
| `POST` | `/subscriptions/checkout-session` | Crear sesion de pago |
| `POST` | `/subscriptions/cancel` | Cancelar suscripcion |
| `POST` | `/subscriptions/reactivate` | Reactivar suscripcion |

### Verificacion Tecnica

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `POST` | `/technical-verification/order` | Solicitar verificacion |
| `POST` | `/technical-verification/accept` | Aceptar verificacion |
| `POST` | `/technical-verification/process` | Procesar verificacion |
| `GET` | `/technical-verification/status/{postId}` | Estado de verificacion |

### Busqueda

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| `GET` | `/search` | Busqueda global |
| `GET` | `/search/users` | Buscar usuarios |
| `GET` | `/search/posts` | Buscar publicaciones |

---

## WebSockets

Conexion para mensajeria en tiempo real:

```
Endpoint:    ws://localhost:8080/ws (con fallback SockJS)
Suscribir:   /topic/messages, /queue/messages
Enviar:      /app/send-message
```

La autenticacion se realiza via JWT durante el handshake de conexion.

---

## Base de Datos

### Entidades Principales

| Entidad | Descripcion |
|---------|-------------|
| `UserEntity` | Usuarios del sistema con roles y creditos |
| `AccountEntity` | Perfil publico del usuario |
| `PostEntity` | Publicaciones de vehiculos |
| `PostImageEntity` | Imagenes de publicaciones (multiples resoluciones) |
| `PostLocationEntity` | Ubicacion de la publicacion |
| `MessageEntity` | Mensajes entre usuarios |
| `ConversationEntity` | Conversaciones de chat |
| `TechnicalVerification` | Verificaciones tecnicas vehiculares |
| `PaymentDetail` | Detalles de pagos con Stripe |
| `FollowEntity` | Relaciones de seguimiento |
| `PushTokenEntity` | Tokens de dispositivo para notificaciones |

La base de datos se inicializa automaticamente con marcas y modelos de vehiculos via `data.sql`.

---

## Roles y Permisos

| Rol | Permisos |
|-----|----------|
| `USER` | Crear publicaciones, enviar mensajes, comentar, seguir usuarios |
| `ADMIN` | Acceso completo a todos los endpoints |
| `PREMIUM` | Creditos de verificacion tecnica, notificaciones personalizadas |

---

> **CINOS** — Conectando personas con el auto que buscan.
