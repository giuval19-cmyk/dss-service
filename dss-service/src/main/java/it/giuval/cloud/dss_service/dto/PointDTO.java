package it.giuval.cloud.dss_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rappresenta un punto sul grafico, etichetta asse x e valore su asse y")
public record PointDTO(
		String name,
		int value) 
{}