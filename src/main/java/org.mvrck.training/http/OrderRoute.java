package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.marshallers.jackson.*;
import akka.http.javadsl.server.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.dto.*;

import java.time.*;

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
          var completionStage = AskPattern.ask(
            ticketStockParent,
            replyTo -> new TicketStockParentActor.ProcessOrder(1,34, 1, replyTo),
            Duration.ofSeconds(3),
            system.scheduler()
          );
          return onSuccess(completionStage, result -> complete(result.toString()));
        })
      )
    );
  }
}
