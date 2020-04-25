package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class TicketStockActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(ActorRef<OrderParentActor.Command> orderParent, int ticketId, int quantity){
    if(quantity < 0)
      throw new RuntimeException("TicketStock quantity cannot be negative");
    else
      return Behaviors.setup(context -> available(context, orderParent, ticketId, quantity));
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> available(ActorContext<Command> context, ActorRef<OrderParentActor.Command> orderParent, int ticketId, int quantity) {
    return Behaviors.receive(Command.class)
      .onMessage(ProcessOrder.class, message -> {
        var decrementedQuantity = quantity - message.quantityDecrementedBy;
        if (decrementedQuantity < 0) {
          message.sender.tell(new OrderActor.ErrorResponse("TicketStock cannot have a negative quantity"));
          return Behaviors.same();
        } else if (decrementedQuantity == 0){
          orderParent.tell(new OrderParentActor.CreateOrder(message.ticketId, message.userId, message.quantityDecrementedBy, message.sender));
          return outOfStock(context, ticketId);
        } else {
          orderParent.tell(new OrderParentActor.CreateOrder(message.ticketId, message.userId, message.quantityDecrementedBy, message.sender));
          return available(context, orderParent, ticketId, decrementedQuantity);
        }
      })
      .build();
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> outOfStock(ActorContext<Command> context, int ticketId) {
    return Behaviors.receive(Command.class)
      .onMessage(ProcessOrder.class, message -> {
        message.sender.tell(new OrderActor.ErrorResponse("Ticket is out of stock"));
        return Behaviors.same();
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Command {}

  public static final class ProcessOrder implements Command {
    public final int ticketId;
    public final int userId;
    public final int quantityDecrementedBy;
    public final ActorRef<OrderActor.Response> sender;

    public ProcessOrder(int ticketId, int userId, int quantityDecrementedBy, ActorRef<OrderActor.Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantityDecrementedBy = quantityDecrementedBy;
      this.sender = sender;
    }
  }
}
