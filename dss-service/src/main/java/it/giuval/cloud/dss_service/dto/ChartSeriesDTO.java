package it.giuval.cloud.dss_service.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rappresenta una serie grafica, con i punti dell'asse y")
public record ChartSeriesDTO(
		String name,
		List<PointDTO> series
		) {}