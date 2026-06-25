# Alertas Medicas RabbitMQ Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the RabbitMQ-backed medical alert flow with logical producers, multiple consumers, Oracle persistence, WebSocket notifications, JSON audit files, and periodic vital-sign summaries.

**Architecture:** Keep one Spring Boot deployment and split responsibilities by service classes/listeners. Alerts are published with routing key `alertas.clinicas` to two queues, so the Oracle/WebSocket consumer and JSON-file consumer both receive the same message. A scheduled producer publishes vital-sign summaries to a separate summary queue.

**Tech Stack:** Spring Boot 3.4.4, Spring AMQP, Spring Scheduling, Spring Data JPA, Jackson, Oracle Cloud, Docker Compose.

---

### Task 1: RabbitMQ Multi-Queue Configuration

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/resources/application-local.properties`
- Modify: `src/main/java/com/medicalapp/medicalapp/config/RabbitMqConfig.java`

- [ ] **Step 1: Add properties**

Add alert and summary queue/routing-key properties with current queue defaults preserved.

- [ ] **Step 2: Add queue and binding beans**

Create durable queues:

```java
Queue alertasOracleQueue(@Value("${medicalapp.rabbitmq.alert-oracle-queue}") String queueName)
Queue alertasArchivosQueue(@Value("${medicalapp.rabbitmq.alert-file-queue}") String queueName)
Queue resumenesSignosQueue(@Value("${medicalapp.rabbitmq.summary-queue}") String queueName)
```

Bind alert queues to `alertas.clinicas` and summary queue to `resumenes.signos-vitales`.

- [ ] **Step 3: Run tests**

Run: `./mvnw test`

Expected: build succeeds.

### Task 2: Alert Publishing From Vital Signs

**Files:**
- Create: `src/main/java/com/medicalapp/medicalapp/service/SignosVitalesAlertProducerService.java`
- Create: `src/test/java/com/medicalapp/medicalapp/service/SignosVitalesAlertProducerServiceTest.java`
- Modify: `src/main/java/com/medicalapp/medicalapp/service/MonitoreoMedicoService.java`

- [ ] **Step 1: Write failing unit test**

Test that an anomalous vital-sign reading produces RabbitMQ alert messages, and a normal reading produces none.

- [ ] **Step 2: Verify red**

Run: `./mvnw -Dtest=SignosVitalesAlertProducerServiceTest test`

Expected: fail because service does not exist.

- [ ] **Step 3: Implement producer service**

Use the same clinical thresholds already present in `MonitoreoMedicoService`, publish `EventoClinicoMessage` to the alert routing key, and set JSON message metadata.

- [ ] **Step 4: Replace direct alert creation**

After saving `SignoVital`, call `signosVitalesAlertProducerService.publicarAlertas(paciente, guardada)` instead of creating `MED_ALERTAS` directly.

- [ ] **Step 5: Verify green**

Run: `./mvnw -Dtest=SignosVitalesAlertProducerServiceTest test`

Expected: tests pass.

### Task 3: Oracle/WebSocket Alert Consumer

**Files:**
- Modify: `src/main/java/com/medicalapp/medicalapp/service/EventoClinicoQueueConsumer.java`
- Modify: `src/main/java/com/medicalapp/medicalapp/service/EventoClinicoProducerService.java`

- [ ] **Step 1: Move listener to alert Oracle queue**

Consume from `${medicalapp.rabbitmq.alert-oracle-queue}` so Oracle/WebSocket is one consumer of the alert fan-out.

- [ ] **Step 2: Route public producer into alert flow**

Publish the existing public endpoint to `${medicalapp.rabbitmq.alert-routing-key}`.

- [ ] **Step 3: Run tests**

Run: `./mvnw test`

Expected: build succeeds.

### Task 4: JSON Audit File Consumer

**Files:**
- Create: `src/main/java/com/medicalapp/medicalapp/service/EventoClinicoArchivoConsumer.java`
- Create: `src/test/java/com/medicalapp/medicalapp/service/EventoClinicoArchivoConsumerTest.java`

- [ ] **Step 1: Write failing unit test**

Test that consuming an alert creates a `.json` file in a configured temp directory with patient, type, severity, value, and timestamp.

- [ ] **Step 2: Verify red**

Run: `./mvnw -Dtest=EventoClinicoArchivoConsumerTest test`

Expected: fail because consumer does not exist.

- [ ] **Step 3: Implement file consumer**

Use `@RabbitListener(queues = "${medicalapp.rabbitmq.alert-file-queue}")`, Jackson, `Files.createDirectories`, and timestamped file names.

- [ ] **Step 4: Verify green**

Run: `./mvnw -Dtest=EventoClinicoArchivoConsumerTest test`

Expected: tests pass.

### Task 5: Periodic Summary Producer And Consumer

**Files:**
- Create: `src/main/java/com/medicalapp/medicalapp/dto/ResumenSignosVitalesMessage.java`
- Create: `src/main/java/com/medicalapp/medicalapp/service/ResumenSignosVitalesProducerService.java`
- Create: `src/main/java/com/medicalapp/medicalapp/service/ResumenSignosVitalesQueueConsumer.java`
- Modify: `src/main/java/com/medicalapp/medicalapp/Application.java`
- Create: `src/test/java/com/medicalapp/medicalapp/service/ResumenSignosVitalesProducerServiceTest.java`

- [ ] **Step 1: Write failing unit test**

Test that the producer builds and sends a summary message from active patients and latest vital signs.

- [ ] **Step 2: Verify red**

Run: `./mvnw -Dtest=ResumenSignosVitalesProducerServiceTest test`

Expected: fail because service/DTO do not exist.

- [ ] **Step 3: Implement summary DTO and producer**

Read active patients, find latest vital signs, build summary, publish JSON to summary routing key. Add `@Scheduled(fixedDelayString = "${medicalapp.summary.interval-ms}")` and guard execution with `${medicalapp.summary.scheduler-enabled}`.

- [ ] **Step 4: Implement summary consumer**

Consume from `${medicalapp.rabbitmq.summary-queue}` and log the received JSON.

- [ ] **Step 5: Enable scheduling**

Add `@EnableScheduling` to `Application`.

- [ ] **Step 6: Verify green**

Run: `./mvnw -Dtest=ResumenSignosVitalesProducerServiceTest test`

Expected: tests pass.

### Task 6: Docker, Docs, And Final Verification

**Files:**
- Modify: `docker-compose.yml`
- Modify: `README.md`
- Modify: `src/test/java/com/medicalapp/medicalapp/ApplicationTests.java` if scheduling/listeners need test-safe properties.

- [ ] **Step 1: Add env vars and volume**

Expose all new env vars and mount `medicalapp-alert-files:/app/hospital-alert-files`.

- [ ] **Step 2: Update README**

Document the new producers, consumers, env vars, Postman/vital-sign test payload, and where JSON files are generated.

- [ ] **Step 3: Run full backend verification**

Run: `./mvnw test`

Expected: build succeeds.

- [ ] **Step 4: Inspect final diff**

Run: `git diff --check && git status --short && git diff --stat`

Expected: no whitespace errors and only intended files changed.

