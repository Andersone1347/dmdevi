package com.dmdev.jdbc.starter.entity;

import java.time.LocalDateTime;
import java.util.List;

public record Flight(Long id,
                     String flightNo,
                     LocalDateTime departureDate,
                     String departureAirportCode,
                     LocalDateTime arrivalDate,
                     String arrivalAirportCode,
                     Integer aircraftId,
                     String status,
                     List<Ticket> tickets) {

}
