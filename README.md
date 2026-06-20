# MedicalApp

Sistema de alertas medicas en tiempo real para monitoreo de pacientes criticos.

## Componentes

- Frontend Angular con login IDaaS mediante Azure AD B2C/MSAL.
- BFF Spring Boot protegido por JWT en `/api/**`.
- Proteccion API Manager configurable en el BFF. Para la entrega final se integrara con AWS API Gateway.
- Persistencia Oracle para pacientes, signos vitales y alertas. Los usuarios viven en el IDaaS, no en Oracle.
- Cola RabbitMQ para eventos clinicos asincronos consumidos por Spring Boot y notificados a Angular por WebSocket.

## Endpoints BFF

- `GET /api/dashboard`: resumen de pacientes, ultimas lecturas y alertas activas.
- `GET /api/alertas`: lista de alertas abiertas.
- `GET /api/eventos-clinicos`: lista de eventos clinicos recibidos desde RabbitMQ.
- `PATCH /api/alertas/{id}/atender`: marca una alerta como atendida.
- `POST /api/signos-vitales`: registra una lectura y genera alertas segun umbrales.
- `POST /public/eventos-clinicos`: productor publico que publica un evento en RabbitMQ.

## Desarrollo local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd frontend
npm ci
npm run start
```

## Variables base

```bash
PORT=8080
ORACLE_USERNAME=ADMIN
ORACLE_PASSWORD=change-me
ORACLE_WALLET_BASE64=
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@dbbooks_high?TNS_ADMIN=/app/wallet
IDP_ISSUER_URI=https://ghdevcompany.b2clogin.com/ac1dbc9b-27a4-4004-a719-42d96af26d37/v2.0/
IDP_JWK_SET_URI=https://ghdevcompany.b2clogin.com/ghdevcompany.onmicrosoft.com/b2c_1_dn-gh/discovery/v2.0/keys
MEDICALAPP_API_MANAGER_REQUIRED=false
MEDICALAPP_API_MANAGER_SUBSCRIPTION_KEY=dev-medicalapp-key
MEDICALAPP_FRONTEND_API_BASE_URL=https://v65ti3zvxj.execute-api.us-east-1.amazonaws.com/deves1
MEDICALAPP_AUTH_SCOPES=openid profile
RABBITMQ_USERNAME=medicalapp
RABBITMQ_PASSWORD=medicalapp
MEDICALAPP_RABBITMQ_QUEUE=medicalapp.eventos-clinicos
MEDICALAPP_RABBITMQ_EXCHANGE=medicalapp.exchange
MEDICALAPP_RABBITMQ_ROUTING_KEY=eventos.clinicos
```

`MEDICALAPP_FRONTEND_API_BASE_URL` controla a que API llama Angular:

- Vacio: usa el BFF directo del mismo dominio, por ejemplo `/api/dashboard`.
- Con AWS API Gateway: usa la URL configurada, por ejemplo `https://v65ti3zvxj.execute-api.us-east-1.amazonaws.com/deves1/api/dashboard`.

`MEDICALAPP_AUTH_SCOPES` debe quedar en `openid profile` para mantener el flujo validado con Azure B2C. El frontend envia el ID token como `Authorization: Bearer <token>`.

## Base de datos

Spring puede crear las tablas `MED_PACIENTES`, `MED_SIGNOS_VITALES`, `MED_ALERTAS` y `MED_EVENTOS_CLINICOS` con `spring.jpa.hibernate.ddl-auto=update`.
Tambien se incluyen scripts explicitos en `database/schema.sql` y `database/seed.sql`.

## RabbitMQ y WebSocket

`docker-compose.yml` levanta RabbitMQ con consola en `http://localhost:15672`.
Credenciales por defecto: `medicalapp / medicalapp`.

Para probar desde la interfaz de RabbitMQ:

1. Entrar a `Exchanges`.
2. Seleccionar `medicalapp.exchange`.
3. Publicar con routing key `eventos.clinicos`.
4. Usar un payload JSON como:

```json
{
  "pacienteId": 1,
  "tipo": "SATURACION_CRITICA",
  "origen": "Monitor UCI",
  "mensaje": "Saturacion de oxigeno bajo 88%",
  "severidad": "ALTA",
  "valor": "88%",
  "fechaEvento": "2026-06-13T14:20:00-04:00"
}
```

Spring Boot consume la cola `medicalapp.eventos-clinicos`, guarda el evento en Oracle y lo transmite a Angular por `/ws/eventos-clinicos`.

### Productor HTTP publico

El endpoint productor no requiere JWT. Responde `202 Accepted` porque el guardado y la
notificacion ocurren de forma asincrona en el consumidor RabbitMQ.

```bash
curl -X POST https://medicalapp-cn.adndigital.cl/public/eventos-clinicos \
  -H 'Content-Type: application/json' \
  -d '{
    "pacienteId": 1,
    "tipo": "SATURACION_CRITICA",
    "origen": "Postman",
    "mensaje": "Saturacion de oxigeno bajo 88%",
    "severidad": "ALTA",
    "valor": "88%"
  }'
```

Flujo completo:

```text
POST publico -> exchange -> cola -> @RabbitListener -> Oracle -> WebSocket -> Angular
```
