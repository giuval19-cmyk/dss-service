package it.giuval.cloud.dss_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.influxdb.v3.client.InfluxDBClient;

import it.giuval.cloud.dss_service.dto.ChartSeriesDTO;
import it.giuval.cloud.dss_service.dto.PointDTO;
import it.giuval.cloud.dss_service.enums.TicketActionEvent;

@Service
public class InfluxQueryService {

	private final InfluxDBClient influxClient;


	public InfluxQueryService(InfluxDBClient influxClient) {
		this.influxClient = influxClient;
	}

	/*Numero di ticket aperti/chiusi per ciascuna sourceApp*/
	public List<ChartSeriesDTO> getTicketsTrendStats(int days, TicketActionEvent event) {
		// Query SQL per raggruppare i ticket in "secchielli" di 1 giorno (1d)
		String sqlQuery = String.format("""
				SELECT 
				    DATE_TRUNC('day', time) AS "day",
				    "sourceApp", 
				    SUM("count") AS "total_count"
				FROM 
				    "ticket_events"
				WHERE 
				    "eventType" = '%s' AND time >= now() - INTERVAL '%d days'
				GROUP BY 
				    1, "sourceApp"
				ORDER BY 
				    1 ASC
				""", event.name(),days);

		try (Stream<Object[]> rowStream = influxClient.query(sqlQuery)) {

			Map<String, List<Object[]>> groupedBySourceApp = rowStream
					.filter(row -> row != null && row[0] != null && row[1] != null && row[2] != null)
					.collect(Collectors.groupingBy(row -> row[1].toString()));//RAGGRUPPIAMO ANCORA PER sourceApp

			return groupedBySourceApp.entrySet().stream()
					.map(entry -> {
						String priority = entry.getKey();
						List<Object[]> trendRows = entry.getValue();

						// TRASFORMAZIONE IN DATAPOINT: L'asse X questa volta è la DATA!
						List<PointDTO> timePoints = extractTrends(trendRows);

						return new ChartSeriesDTO(priority, timePoints);
					})
					.toList();
		}
	}

	private List<PointDTO> extractTrends(List<Object[]> trendRows) {
		return trendRows.stream()
				.map(row -> {
					// InfluxDB restituisce il tempo come stringa ISO (es. "2026-05-19T00:00:00Z")
					// Prendiamo solo i primi 10 caratteri per avere la data pulita "YYYY-MM-DD"
					String cleanDate = row[0].toString().substring(0, 10);
					return new PointDTO(
							cleanDate,                       // Asse X: Il giorno (es. "2026-05-19")
							((Number) row[2]).intValue()   // Asse Y: Il totale di quel giorno
							);
				})
				.toList();
	}

}