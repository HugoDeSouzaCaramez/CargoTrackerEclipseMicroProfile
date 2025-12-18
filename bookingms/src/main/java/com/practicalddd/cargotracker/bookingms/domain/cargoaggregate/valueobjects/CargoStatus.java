package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

public enum CargoStatus {
    BOOKED,          // Carga reservada
    ROUTED,          // Rota atribuída
    IN_PORT,         // No porto de origem/transbordo
    IN_TRANSIT,      // A bordo do carrier
    READY_FOR_CLAIM, // No porto de destino, pronto para retirada
    CLAIMED,         // Retirada pelo destinatário
    COMPLETED,       // Entrega finalizada
    CANCELLED,       // Cancelada
    MISDIRECTED      // Desviada/Perdida
}
