# LabMetricas Backend

Sistema de gestión de laboratorio de métricas con Spring Boot.

## 🚀 Despliegue Rápido

Para desplegar en Render, sigue la guía completa en [DEPLOYMENT.md](./DEPLOYMENT.md).

### Configuración Mínima en Render:

**Build Command:**
```bash
./mvnw clean package -DskipTests
```

**Start Command:**
```bash
java -jar target/LabMetricas-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Variables de Entorno Requeridas:**
- `DATABASE_URL`
- `DATABASE_USERNAME` 
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `RESEND_API_KEY`

## 🛠️ Desarrollo Local

```bash
# Clonar repositorio
git clone <tu-repo>

# Ejecutar con Maven
./mvnw spring-boot:run

# O con Java directamente
./mvnw clean package
java -jar target/LabMetricas-0.0.1-SNAPSHOT.jar
```

## 📋 Características

- ✅ Autenticación JWT
- ✅ Gestión de usuarios y roles
- ✅ Sistema de emails con Resend
- ✅ API RESTful
- ✅ Base de datos PostgreSQL
- ✅ Logs y auditoría
- ✅ Health checks

## 🔧 Tecnologías

- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Resend (Email)
- Maven
