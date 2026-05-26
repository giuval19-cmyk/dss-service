package it.giuval.cloud.dss_service.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;

import it.giuval.cloud.dss_service.dto.TicketEventDTO;
import it.giuval.cloud.dss_service.enums.TicketActionEvent;

@Service
public class TicketConsumerService {

	private final static Logger log= LoggerFactory.getLogger(TicketConsumerService.class);

	private final InfluxDBClient influxClient;
	private final NotificationService notificationService;

	public TicketConsumerService(InfluxDBClient influxClient, NotificationService notificationService) {
		this.influxClient = influxClient;
		this.notificationService = notificationService;
	}

	public void handleIncomingTicket(TicketEventDTO ticket) {

		try {
			validateTicket(ticket);

			processAndSave(ticket);

			if(ticket.eventType()==TicketActionEvent.OPEN) {
				notificationService.emitNotification(ticket);
			}

			log.info("Ticket {} elaborato con successo", ticket.id());
		} catch (IllegalArgumentException e) {
			log.error("Validazione fallita per il ticket {}: {}", ticket.id(), e.getMessage());
			throw new AmqpRejectAndDontRequeueException(e.getMessage()); // Lo manda in DLQ istantaneamente

		} catch (Exception e) {
			log.warn("Errore temporaneo durante l'elaborazione del ticket {}. Tentativo di retry...", ticket.id());
			throw e; // Rilanciando l'eccezione, scatta il retry (max 5 volte) configurato nelle properties
		}

	}

	private void validateTicket(TicketEventDTO ticket) {
		if (ticket.id() == null || ticket.subject() == null) {
			throw new IllegalArgumentException("Campi obbligatori mancanti (ID o Oggetto)");
		}
	}

	private void processAndSave(TicketEventDTO ticket) {
		Point point = Point.measurement("ticket_events")
				.setTag("sourceApp", ticket.sourceApp())
				.setTag("eventType", ticket.eventType().name()) // Ottimo per filtrare nei grafici
				.setField("count", 1L)// Sommiamo 1 per ogni arrivo
				.setTimestamp(Instant.now());

		influxClient.writePoint(point);
	}
}