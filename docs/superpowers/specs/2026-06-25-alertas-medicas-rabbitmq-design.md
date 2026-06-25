# Alertas Medicas RabbitMQ Design

## Objetivo

Ampliar MedicalApp para demostrar un sistema de alertas medicas en tiempo real para pacientes criticos, usando RabbitMQ como broker, Spring Boot como backend, Oracle Cloud como persistencia y archivos JSON para auditoria hospitalaria.

## Alcance

El proyecto mantendra un solo despliegue Spring Boot, pero separara las responsabilidades en componentes logicos equivalentes a microservicios:

- Productor 1: recibe signos vitales desde dispositivos medicos, detecta anomalias y publica alertas en RabbitMQ.
- Productor 2: genera periodicamente un resumen de signos vitales y lo publica en RabbitMQ.
- Consumidor 1: consume alertas, las guarda en Oracle Cloud y las envia por WebSocket a Angular.
- Consumidor 2: consume las mismas alertas y genera archivos `.json` en una ruta configurable del sistema de archivos.

## Arquitectura RabbitMQ

Se usara el exchange durable existente de MedicalApp y se ampliara con nuevas colas y routing keys.

```text
medicalapp.exchange

routing key: alertas.clinicas
  -> medicalapp.alertas.oracle
  -> medicalapp.alertas.archivos

routing key: resumenes.signos-vitales
  -> medicalapp.resumenes-signos
```

La separacion en dos colas de alerta permite que ambos consumidores reciban una copia del mismo mensaje: uno para Oracle y WebSocket, otro para archivo JSON.

## Flujo De Alertas

1. Un dispositivo medico envia signos vitales al endpoint existente `POST /api/signos-vitales`.
2. El backend valida y guarda la lectura en `MED_SIGNOS_VITALES`.
3. El Productor 1 evalua rangos clinicos:
   - Saturacion menor a 90.
   - Frecuencia cardiaca menor a 45 o mayor a 130.
   - Presion sistolica menor a 90 o mayor a 180.
   - Temperatura mayor o igual a 38.5.
   - Frecuencia respiratoria menor a 8 o mayor a 30.
4. Si existe anomalia, publica uno o mas mensajes de alerta con routing key `alertas.clinicas`.
5. Consumidor 1 guarda el evento en `MED_EVENTOS_CLINICOS`, crea una alerta en `MED_ALERTAS` cuando corresponde y notifica a Angular por `/ws/eventos-clinicos`.
6. Consumidor 2 serializa el mismo mensaje con Jackson y genera un archivo `.json` en la ruta configurada.

## Flujo De Resumen Periodico

1. Una tarea programada se ejecuta cada 5 minutos por defecto.
2. Lee pacientes activos y sus ultimos signos vitales.
3. Publica un mensaje de resumen en RabbitMQ con routing key `resumenes.signos-vitales`.
4. Un consumidor de resumen registra el mensaje en logs para demostrar la recepcion asincrona.

El resumen no requiere una nueva tabla en Oracle porque el historico clinico ya queda en `MED_SIGNOS_VITALES`; el objetivo academico es demostrar el Productor 2 y la cola de resumenes.

## Configuracion Por Environment

Se agregaran estas variables al backend y al `docker-compose.yml`:

```env
MEDICALAPP_RABBITMQ_ALERT_ORACLE_QUEUE=medicalapp.alertas.oracle
MEDICALAPP_RABBITMQ_ALERT_FILE_QUEUE=medicalapp.alertas.archivos
MEDICALAPP_RABBITMQ_ALERT_ROUTING_KEY=alertas.clinicas
MEDICALAPP_RABBITMQ_SUMMARY_QUEUE=medicalapp.resumenes-signos
MEDICALAPP_RABBITMQ_SUMMARY_ROUTING_KEY=resumenes.signos-vitales
MEDICALAPP_ALERT_FILES_PATH=/app/hospital-alert-files
MEDICALAPP_SUMMARY_SCHEDULER_ENABLED=true
MEDICALAPP_SUMMARY_INTERVAL_MS=300000
```

El contenedor `medicalapp` montara un volumen persistente en `/app/hospital-alert-files` para conservar los archivos JSON generados.

## Contratos De Mensaje

Las alertas usaran el contrato existente `EventoClinicoMessage`, porque ya contiene paciente, tipo, origen, mensaje, severidad, valor y fecha del evento.

Los resumenes usaran un nuevo DTO con:

- `pacientesActivos`
- `lecturasIncluidas`
- `fechaResumen`
- lista de pacientes con su ultimo signo vital disponible

## Compatibilidad Con Lo Existente

El endpoint publico `POST /public/eventos-clinicos` seguira funcionando y publicara en el flujo de alertas.

La vista Angular actual de "Eventos en vivo" seguira consumiendo el mismo WebSocket. No se requiere redisenar la pantalla para cumplir esta experiencia.

## Manejo De Errores

- Mensajes invalidos en consumidores se rechazaran lanzando excepcion para que RabbitMQ aplique el comportamiento estandar del listener.
- La escritura de archivos creara el directorio si no existe.
- Los nombres de archivos incluiran timestamp, paciente y tipo para evitar colisiones.
- Si no hay pacientes o lecturas, el resumen periodico publicara un resumen vacio pero valido, dejando evidencia en logs.

## Pruebas

La verificacion minima sera:

- `./mvnw test`
- Publicar signos vitales anomalos por API y comprobar que:
  - RabbitMQ recibe y enruta el mensaje.
  - Oracle guarda el evento y la alerta.
  - Angular recibe el evento por WebSocket.
  - Se genera un archivo `.json`.
- Verificar que la tarea de resumen publica mensajes segun el intervalo configurado.

