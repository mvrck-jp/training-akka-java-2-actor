package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.*;
import akka.stream.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.http.*;

public class GuardianBehavior {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  public static Behavior<GuardianActor.Message> create() {
    return Behaviors.setup(context -> {
      /*********************************************************************************
       * Set up actor hierarchy on startup
       *********************************************************************************/
      var orderParent = context.spawn(OrderParentActor.create(), "order-parent");
      var ticketStockParent = context.spawn(TicketStockParentActor.create(orderParent), "ticket-stock-parent");

      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(1, 25210));
      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(2, 10));
      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(3, 10));
      ticketStockParent.tell(new TicketStockParentActor.CreateTicketStock(4, 100));

      /*********************************************************************************
       * Set up HTTP server
       *********************************************************************************/
      var materializer = Materializer.createMaterializer(context.getSystem());

      var http = Http.get(context.getSystem().classicSystem());
      var allRoute = new AllRoute(context.getSystem(), ticketStockParent);
      var routeFlow = allRoute.route().flow(context.getSystem().classicSystem(), materializer);
      var binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

      // Shutdown behavior
      return Behaviors.receive(GuardianActor.Message.class)
        .onMessage(GuardianActor.TerminateHttp.class, message -> {
            binding
              .thenCompose(ServerBinding::unbind)
              .thenAccept(unbound -> context.getSystem().terminate());
            return Behaviors.empty();
          }
        ).build();
    });
  }

  // Actor Messages are in GuardianActor
}
