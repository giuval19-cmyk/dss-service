package it.giuval.cloud.dss_service;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.giuval.cloud.dss_service.dto.TicketEventDTO;
import it.giuval.cloud.dss_service.service.TicketConsumerService;

@Configuration
public class RabbitMQConfiguration {

	@Bean
    public Consumer<TicketEventDTO> receiveTicket(TicketConsumerService ticketService) {
        // Ogni volta che arriva un messaggio, chiamiamo il metodo del service
        return payload -> ticketService.handleIncomingTicket(payload);
    }
}
