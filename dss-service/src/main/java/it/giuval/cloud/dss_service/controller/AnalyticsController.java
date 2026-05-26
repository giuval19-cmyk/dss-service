package it.giuval.cloud.dss_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.giuval.cloud.dss_service.dto.ChartSeriesDTO;
import it.giuval.cloud.dss_service.enums.TicketActionEvent;
import it.giuval.cloud.dss_service.service.InfluxQueryService;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Endpoint per il monitoraggio dei ticket su InfluxDB")
public class AnalyticsController {

	private final InfluxQueryService queryService;

	public AnalyticsController(InfluxQueryService queryService) {
		this.queryService = queryService;
	}

	@Operation(
			summary = "Ottieni il trend dei ticket aperti per applicazione", 
			description = "Restituisce lo storico temporale dei ticket aperti negli ultimi N giorni, raggruppati per applicazione di provenienza. Ottimo per grafici a linee."
			)
	@GetMapping("/open-trends")
	public ResponseEntity<List<ChartSeriesDTO>> getOpenTrend(@RequestParam(defaultValue = "7") int days) {
		return ResponseEntity.ok(queryService.getTicketsTrendStats(days,TicketActionEvent.OPEN));
	}

	@Operation(
			summary = "Ottieni il trend dei ticket chiusi per applicazione", 
			description = "Restituisce lo storico temporale dei ticket chiusi (evasi) negli ultimi N giorni, raggruppati per applicazione di provenienza. Utile per monitorare le performance del team."
			)
	@GetMapping("/close-trends")
	public ResponseEntity<List<ChartSeriesDTO>> getCloseTrend(@RequestParam(defaultValue = "7") int days) {
		return ResponseEntity.ok(queryService.getTicketsTrendStats(days,TicketActionEvent.CLOSE));
	}
}