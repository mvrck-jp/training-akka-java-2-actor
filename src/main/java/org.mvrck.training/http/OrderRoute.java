package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.marshallers.jackson.*;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.dto.*;

import java.time.*;
import java.util.concurrent.*;

public class OrderRoute extends AllDirectives {
  ActorRef<TicketStockParentActor.Message> ticketStockParent;
  ActorSystem<Void> system;

  public OrderRoute(
    ActorSystem<Void> system,
    ActorRef<TicketStockParentActor.Message> ticketStockParent){
    this.system = system;
    this.ticketStockParent = ticketStockParent;
  }

  public Route route(){
    return pathPrefix("orders", () ->
      pathEndOrSingleSlash(() ->
        entity(Jackson.unmarshaller(OrderPutRequest.class), req -> {
          CompletionStage<OrderActor.Response> completionStage = AskPattern.ask(
            ticketStockParent,
            replyTo -> new TicketStockParentActor.ProcessOrder(req.getTicketId(),req.getUserId(), req.getQuantity(), replyTo),
            Duration.ofSeconds(3),
            system.scheduler()
          );
          return onSuccess(completionStage, result -> complete(StatusCodes.OK, OrderPutResponse.convert(result), Jackson.marshaller()));
        })
      )
    );
  }
}
