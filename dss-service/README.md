# 📊 DSS Service (Decision Support System)

Il **DSS Service** è il componente analitico dell'ecosistema, progettato per agire come sistema di supporto alle decisioni. È un microservizio completamente asincrono ed **Event-Driven** che consuma i dati operativi, ne traccia l'evoluzione temporale e applica logiche di telemetria e resilienza di livello Enterprise.

---

## 🚀 Funzionalità Principali & Competenze Dimostrate

* **Architettura Event-Driven (Reactive Consumer):** Il servizio non espone API di scrittura sincrone. Rimane in ascolto degli eventi di business (es. la creazione di ticket di supporto) pubblicati sul Message Broker, elaborandoli in background senza impattare sulle performance dei servizi core.
* **Integrazione con Spring Cloud Stream:** Implementa la comunicazione asincrona sfruttando l'approccio funzionale di Spring Cloud Stream (`java.util.function.Consumer`), astraendo la logica di business dal broker sottostante.
* **Database Temporale (InfluxDB Cloud):** Per l'analisi dei dati e della telemetria, il servizio utilizza **InfluxDB**, un database NoSQL ottimizzato per serie temporali (*Time-Series*). Permette di tracciare metriche sui ticket, tempi di risoluzione e trend prestazionali aggregati nel tempo.
* **Service Discovery (Eureka Client):** Si registra autonomamente sull'interfaccia di **Eureka** per segnalare il proprio stato di salute e permettere il monitoraggio distribuito da parte del Gateway.

---

## 🛡️ Meccanismi di Fault Tolerance & Resilienza (DLQ)

In un sistema distribuito, i sistemi di messaggistica possono fallire a causa di dati malformati o downtime dei database. Il DSS Service implementa una strategia di tolleranza ai guasti robusta ed elegante su **RabbitMQ (CloudAMQP)**:

1. **Stateful Retry (Back-off Progressivo):** Se l'elaborazione di un messaggio fallisce (ad esempio, InfluxDB è momentaneamente irraggiungibile), Spring Cloud Stream non scarta il messaggio, ma tenta di rielaborarlo automaticamente per un massimo di **5 volte**, raddoppiando il tempo di attesa tra un tentativo e l'altro (*exponential back-off*).
2. **Dead Letter Queue (DLQ):** Se anche l'ultimo tentativo fallisce, il messaggio viene rimosso dalla coda principale per evitare di bloccare il traffico (*poison pill*) e viene spostato automaticamente in una coda dedicata agli errori: 
   `FAILED.dss-service.support-tickets.errors`
   Questo permette agli amministratori di analizzare il payload scartato e rigenerarlo in un secondo momento senza perdita di dati.

---

## 🛠️ Configurazione Centralizzata GitOps

Il modulo delega interamente la gestione dei propri endpoint sensibili al **Config Server**. Il file `application.properties` locale contiene esclusivamente le istruzioni di bootstrap:

```properties
spring.application.name=dss-service
server.port=8082