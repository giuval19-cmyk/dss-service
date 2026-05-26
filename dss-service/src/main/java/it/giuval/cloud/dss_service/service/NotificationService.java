package it.giuval.cloud.dss_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.giuval.cloud.dss_service.dto.TicketEventDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class NotificationService {
	
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	// Il Sink funge da "distributore automatico". 
    // multicast() dice a Spring che molti client ascolteranno lo stesso flusso.
    // onBackpressureBuffer() evita di mandare in crash il server se un client è lento.
    private final Sinks.Many<String> jsonSink = Sinks.many().multicast().onBackpressureBuffer();
    private final ObjectMapper objectMapper;
    
    public NotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * Questo metodo riceve il DTO, lo trasforma in stringa JSON 
     * e lo spara nel tunnel verso tutti gli Angular connessi.
     */
    public void emitNotification(TicketEventDTO ticketEvent) {
        try {
            String json = objectMapper.writeValueAsString(ticketEvent);
            jsonSink.tryEmitNext(json);
            log.info("Evento:"+ticketEvent.id()+" notificato");
        } catch (Exception e) {
        	e.printStackTrace();
            System.err.println("Errore conversione DTO in stringa nel DSS: " + e.getMessage());
        }
    }
    
    /**
     * Questo metodo restituisce il flusso a cui si collegheranno i controller.
     */
    public Flux<String> getGlobalFlux() {
        return jsonSink.asFlux();
    }
}
