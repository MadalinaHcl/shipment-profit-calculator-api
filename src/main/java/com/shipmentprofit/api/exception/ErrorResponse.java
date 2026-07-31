package com.shipmentprofit.api.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp, int status, String message, List<String> details) {

  public ErrorResponse(int status, String message, List<String> details) {
    this(LocalDateTime.now(), status, message, details);
  }
}
