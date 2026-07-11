# MedicalApp

Sistema de alertas medicas en tiempo real para monitoreo de pacientes criticos, construido sobre un
**cluster Apache Kafka** con un pipeline de microservicios (productor -> procesador -> alerta).

## Componentes

- Frontend Angular con login IDaaS mediante Azure AD B2C/MSAL.
- BFF Spring Boot protegido por JWT en `/api/**`.
- Proteccion API Manager configurable en el BFF.
- Persistencia Oracle para pacientes, signos vitales y alertas. Los usuarios viven en el IDaaS, no en Oracle.
- **Cluster Kafka de 3 brokers (modo KRaft)** para el flujo de señales vitales, deteccion de anomalias,
  persistencia Oracle, archivos JSON de auditoria y notificaciones Angular por WebSocket.

## Arquitectura de microservicios (Kafka)

Los tres roles del enunciado conviven dentro del mismo servicio Spring Boot y se comunican por topicos Kafka:

```text
Simulador (@1s) ─┐
POST /signos ────┴─► topic: senales_vitales ─► Procesador ─► topic: alertas ─┬─► Consumidor Oracle + WebSocket ─► Angular
                                              (persiste +                    └─► Consumidor archivo JSON (auditoria)
                                               detecta anomalias)
```

- **Productor de señales vitales** — `SimuladorSignosVitalesProducer`: genera lecturas (frecuencia cardiaca,
  presion, saturacion, temperatura, frecuencia respiratoria) para cada paciente activo **cada segundo** y las
  publica en `senales_vitales`.
- **Procesador de señales** — `SenalesVitalesProcessor`: consume `senales_vitales`, persiste la lectura y
  detecta valores anomalos (`DetectorAnomalias`). Cuando hay anomalias, las publica en `alertas`.
- **Servicio de alerta** — dos consumer groups sobre `alertas`:
  - `AlertasOracleConsumer`: guarda el evento y la alerta en Oracle Cloud y notifica al dashboard por WebSocket.
  - `AlertasArchivoConsumer`: genera un archivo JSON de auditoria por cada alerta.

> Nota: Kafka solo admite `[a-zA-Z0-9._-]` en nombres de topico, por lo que el `señales_vitales` del enunciado
> se implementa como **`senales_vitales`**.

## Topicos Kafka

| Topico | Rol |
|--------|-----|
| `senales_vitales` | Lecturas de señales vitales publicadas por el productor. |
| `alertas` | Lecturas anomalas detectadas por el procesador. |
| `resumenes-signos` | Resumen periodico de pacientes activos (feature auxiliar). |

Los topicos se crean automaticamente al arrancar (`KafkaConfig` + `KafkaAdmin`) con las particiones y el factor
de replicacion configurados (`3` en el cluster, `1` en local).

## Endpoints BFF

- `GET /api/dashboard`: resumen de pacientes, ultimas lecturas y alertas activas.
- `GET /api/alertas`: lista de alertas abiertas.
- `GET /api/eventos-clinicos`: lista de eventos clinicos recibidos desde Kafka.
- `PATCH /api/alertas/{id}/atender`: marca una alerta como atendida.
- `POST /api/signos-vitales` / `POST /public/signos-vitales`: registra una lectura, detecta anomalias y publica alertas en Kafka.
- `POST /public/eventos-clinicos`: productor publico que publica un evento en el topico `alertas`.
- `GET /public/archivos-alertas`: lista los archivos JSON de auditoria generados.

## Desarrollo local

Requiere un broker Kafka en `localhost:9092`. Con Homebrew:

```bash
brew install kafka
brew services start kafka      # o: /opt/homebrew/opt/kafka/bin/kafka-server-start ...
```

Luego:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd frontend
npm ci
npm run start
```

El perfil `local` usa H2 en memoria y un solo broker (replicacion/particiones = 1). El simulador queda activo
por defecto, generando señales cada segundo.

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
MEDICALAPP_FRONTEND_API_BASE_URL=
MEDICALAPP_AUTH_SCOPES=openid profile
KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092
KAFKA_REPLICATION_FACTOR=3
KAFKA_PARTITIONS=3
MEDICALAPP_KAFKA_TOPIC_SENALES=senales_vitales
MEDICALAPP_KAFKA_TOPIC_ALERTAS=alertas
MEDICALAPP_KAFKA_TOPIC_RESUMENES=resumenes-signos
MEDICALAPP_SIMULATOR_ENABLED=true
MEDICALAPP_SIMULATOR_INTERVAL_MS=1000
MEDICALAPP_ALERT_FILES_PATH=/app/hospital-alert-files
MEDICALAPP_SUMMARY_SCHEDULER_ENABLED=true
MEDICALAPP_SUMMARY_INTERVAL_MS=300000
```

## Base de datos

Spring puede crear las tablas `MED_PACIENTES`, `MED_SIGNOS_VITALES`, `MED_ALERTAS` y `MED_EVENTOS_CLINICOS`
con `spring.jpa.hibernate.ddl-auto=update`. Tambien se incluyen scripts en `database/schema.sql` y `database/seed.sql`.

## Cluster Kafka (3 nodos)

`docker-compose.yml` levanta un cluster de 3 brokers Kafka en modo KRaft (`kafka-1`, `kafka-2`, `kafka-3`),
sin Zookeeper, con `replication-factor=3` y `min.insync.replicas=2`.

```bash
docker compose up --build
```

Para inspeccionar los topicos y la distribucion de replicas entre los 3 brokers:

```bash
docker exec medicalapp-kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic senales_vitales
docker exec medicalapp-kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --describe --topic alertas
```

### Publicar una alerta manualmente

```bash
curl -X POST http://localhost:8080/public/eventos-clinicos \
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
POST publico -> topic alertas -> @KafkaListener -> Oracle -> WebSocket -> Angular
                                              \-> archivo JSON de auditoria
```

## Despliegue en Dokploy

El despliegue se dispara por `git push` (CI/CD en Dokploy). Para el cluster Kafka, desplegar el
`docker-compose.yml` (3 brokers + app) como stack **Compose** en Dokploy; Dokploy reconstruye la imagen de la
app con `build: .`. Alternativamente, correr los 3 brokers como stack aparte y pasar `KAFKA_BOOTSTRAP_SERVERS`
por variable de entorno. El manejo del wallet Oracle (`ORACLE_WALLET_BASE64`) y `entrypoint.sh` no cambian.
