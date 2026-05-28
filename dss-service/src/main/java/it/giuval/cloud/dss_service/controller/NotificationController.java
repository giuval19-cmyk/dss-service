package it.giuval.cloud.dss_service.controller;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.giuval.cloud.dss_service.service.NotificationService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

	@Autowired
	private NotificationService notificationService;
	/**
	 * Questo endpoint apre il tunnel SSE con Angular.
	 * Il produttore dichiara il MediaType specifico TEXT_EVENT_STREAM_VALUE.
	 */
	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> streamNotifications(
			@RequestHeader("X-User-Id") String userId) {

		log.info("Utente " + userId + " connesso al canale di notifiche SSE!");
		// Trasformiamo le stringhe del servizio in eventi SSE standard per il browser
		Flux<ServerSentEvent<String>> realNotifications = notificationService.getGlobalFlux()
				.map(data -> ServerSentEvent.<String>builder()
						.event("message")
						.data(data)
						.build());
		
		Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(20))
				.map(i -> ServerSentEvent.<String>builder()
						.comment("keep-alive") 
						.build());
		
		return Flux.merge(realNotifications, heartbeat)
				.doOnCancel(() -> {
					System.out.println("NOTIFICA BACKEND: Un client si è disconnesso dal canale SSE (doOnCancel)!");
				})
				.doOnError(e -> {
					System.out.println("Connessione interrotta bruscamente: " + e.getMessage());
				});
	}
}
