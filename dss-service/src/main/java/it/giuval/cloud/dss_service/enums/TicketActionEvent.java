package it.giuval.cloud.dss_service.enums;

public enum TicketActionEvent {
	OPEN,
	TAKE_CHARGE,  // Porta da OPEN a IN_PROGRESS
    RESOLVE,      // Porta da IN_PROGRESS a RESOLVED
    CLOSE         // Porta da RESOLVED a CLOSED
}
