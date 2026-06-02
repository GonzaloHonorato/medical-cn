# MedicalApp

Sistema de alertas medicas en tiempo real para monitoreo de pacientes criticos.

## Componentes

- Frontend Angular con login IDaaS mediante Azure AD B2C/MSAL.
- BFF Spring Boot protegido por JWT en `/api/**`.
- Proteccion API Manager configurable en el BFF. Para la entrega final se integrara con AWS API Gateway.
- Persistencia Oracle para pacientes, signos vitales y alertas. Los usuarios viven en el IDaaS, no en Oracle.

## Endpoints BFF

- `GET /api/dashboard`: resumen de pacientes, ultimas lecturas y alertas activas.
- `GET /api/alertas`: lista de alertas abiertas.
- `PATCH /api/alertas/{id}/atender`: marca una alerta como atendida.
- `POST /api/signos-vitales`: registra una lectura y genera alertas segun umbrales.

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
```

## Base de datos

Spring puede crear las tablas `MED_PACIENTES`, `MED_SIGNOS_VITALES` y `MED_ALERTAS` con `spring.jpa.hibernate.ddl-auto=update`.
Tambien se incluyen scripts explicitos en `database/schema.sql` y `database/seed.sql`.
