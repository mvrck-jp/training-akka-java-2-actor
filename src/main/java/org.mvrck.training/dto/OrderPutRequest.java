package org.mvrck.training.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderPutRequest {
  private final int ticketId;
  private final int userId;

  @JsonCreator
  public OrderPutRequest(@JsonProperty("ticket_id") int ticketId, @JsonProperty("user_id") int userId) {
    this.ticketId = ticketId;
    this.userId = userId;
  }

  public int getTicketId() {
    return ticketId;
  }

  public int getUserId() {
    return userId;
  }
}
