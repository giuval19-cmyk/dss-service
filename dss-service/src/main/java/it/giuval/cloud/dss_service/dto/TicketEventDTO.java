package it.giuval.cloud.dss_service.dto;

import it.giuval.cloud.dss_service.enums.TicketActionEvent;
/**
 * DTO per gli eventi RabbitMQ. 
 */
public record TicketEventDTO(
		String id,
		String subject,
		String userEmail,
		String sourceApp,
		TicketActionEvent eventType
		) {}