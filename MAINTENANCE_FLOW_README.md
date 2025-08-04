# Flujo de Mantenimiento - LabMétricas Backend

## Descripción General

Este documento describe el flujo completo de mantenimiento implementado en el sistema LabMétricas. El flujo incluye creación, revisión, aprobación y rechazo de mantenimientos.

## Estados del Mantenimiento

### ReviewStatus Enum
- **IN_PROGRESS**: En progreso (automático al crear)
- **PENDING**: Pendiente de revisión por el creador
- **APPROVED**: Aprobado por el creador
- **REJECTED**: Rechazado por el creador

## Flujo Completo

### 1. Creación de Mantenimiento
**Endpoint**: `POST /api/maintenance`
- Crea un nuevo mantenimiento
- Estado inicial: `IN_PROGRESS` (automático)
- Se asigna automáticamente el `requestedBy` (creador)
- Se notifica automáticamente al responsable

### 2. Envío para Revisión
**Endpoint**: `POST /api/maintenance/submit-for-review`
- El **responsable** envía el mantenimiento para revisión al **creador**
- Estado: `PENDING`
- Se notifica automáticamente al creador

### 3. Aprobación
**Endpoint**: `POST /api/maintenance/approved/{maintenanceId}`
- El **creador** aprueba el mantenimiento
- Estado: `APPROVED`
- Se notifica automáticamente al responsable

### 4. Rechazo
**Endpoint**: `POST /api/maintenance/rejected/{maintenanceId}`
- El **creador** rechaza el mantenimiento con razón
- Estado: `IN_PROGRESS` (vuelve a progreso para que pueda intentarlo de nuevo)
- Se notifica automáticamente al responsable con la razón del rechazo

## Endpoints Disponibles

### Creación y Gestión
- `POST /api/maintenance` - Crear mantenimiento
- `PUT /api/maintenance/{maintenanceId}` - Actualizar mantenimiento
- `DELETE /api/maintenance/{maintenanceId}` - Eliminación lógica

### Flujo de Aprobación
- `POST /api/maintenance/submit-for-review` - Enviar para revisión
- `POST /api/maintenance/approved/{maintenanceId}` - Aprobar mantenimiento
- `POST /api/maintenance/rejected/{maintenanceId}` - Rechazar mantenimiento

### Consultas
- `GET /api/maintenance` - Todos los mantenimientos
- `GET /api/maintenance/{maintenanceId}` - Mantenimiento específico
- `GET /api/maintenance/status/{reviewStatus}` - Por estado de revisión
- `GET /api/maintenance/responsible/{userId}` - Por usuario responsable
- `GET /api/maintenance/requested/{userId}` - Por usuario solicitante
- `GET /api/maintenance/pending-review` - Pendientes de revisión
- `GET /api/maintenance/approved` - Aprobados
- `GET /api/maintenance/in-progress` - En progreso
- `GET /api/maintenance/rejected` - Rechazados

## DTOs Utilizados

### MaintenanceRequestDto
```json
{
  "equipmentId": "uuid",
  "maintenanceTypeId": "uuid",
  "responsibleUserId": "uuid",
  "description": "string",
  "priority": "LOW|MEDIUM|HIGH|CRITICAL"
}
```

### MaintenanceSubmitForReviewDto
```json
{
  "maintenanceId": "uuid"
}
```

### MaintenanceRejectionDto
```json
{
  "rejectionReason": "string (obligatorio)"
}
```

## Notificaciones

El sistema envía notificaciones automáticas en los siguientes casos:

1. **Asignación**: Se notifica al responsable cuando se crea un mantenimiento
2. **Solicitud de Revisión**: Se notifica al creador cuando se envía para revisión
3. **Aprobación**: Se notifica al responsable cuando se aprueba
4. **Rechazo**: Se notifica al responsable con la razón del rechazo

## Validaciones

### Creación
- Equipo debe existir
- Tipo de mantenimiento debe existir
- Usuario responsable debe existir

### Envío para Revisión
- Solo el responsable puede enviar para revisión
- Solo mantenimientos `IN_PROGRESS` pueden enviarse para revisión

### Aprobación
- Solo mantenimientos `PENDING` pueden ser aprobados
- Solo el creador puede aprobar

### Rechazo
- Solo mantenimientos `PENDING` pueden ser rechazados
- Solo el creador puede rechazar
- **`rejectionReason` es OBLIGATORIO**
- Al rechazar, vuelve a `IN_PROGRESS` para que pueda intentarlo de nuevo

## Base de Datos

### Campos en Tabla `maintenance`
- `review_status`: ENUM con estados de revisión
- `rejection_reason`: VARCHAR(500) para razón de rechazo
- `requested_by`: BINARY(16) FK a user (creador)
- `reviewed_by`: BINARY(16) FK a user (revisor)
- `reviewed_at`: TIMESTAMP de revisión

### Índices
- `idx_maintenance_review_status`
- `idx_maintenance_requested_by`
- `idx_maintenance_reviewed_by`

## Ejemplo de Uso

### 1. Crear Mantenimiento
```bash
POST /api/maintenance
{
  "equipmentId": "123e4567-e89b-12d3-a456-426614174000",
  "maintenanceTypeId": "123e4567-e89b-12d3-a456-426614174001",
  "responsibleUserId": "123e4567-e89b-12d3-a456-426614174002",
  "description": "Mantenimiento preventivo del equipo",
  "priority": "MEDIUM"
}
```

### 2. Enviar para Revisión
```bash
POST /api/maintenance/submit-for-review
{
  "maintenanceId": "123e4567-e89b-12d3-a456-426614174003"
}
```

### 3. Aprobar Mantenimiento
```bash
POST /api/maintenance/approved/123e4567-e89b-12d3-a456-426614174003
```

### 4. Rechazar Mantenimiento
```bash
POST /api/maintenance/rejected/123e4567-e89b-12d3-a456-426614174003
{
  "rejectionReason": "Falta información adicional requerida"
}
``` 