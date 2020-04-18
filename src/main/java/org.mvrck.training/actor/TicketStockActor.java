package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class TicketStockActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Message> create(ActorRef<OrderParentActor.Message> orderParent, int ticketId, int quantity){
    if(quantity < 0)
      throw new RuntimeException("TicketStock quantity cannot be negative");
    else
      return Behaviors.setup(context -> available(context, orderParent, ticketId, quantity));
  }

  // private: never accessed from outside the actor
  private static Behavior<Message> available(ActorContext<Message> context, ActorRef<OrderParentActor.Message> orderParent, int ticketId, int quantity) {
    return Behaviors.receive(Message.class)
      .onMessage(ProcessOrder.class, message -> {
        var decrementedQuantity = quantity - message.quantityDecrementedBy;
        if (decrementedQuantity < 0) {
          message.sender.tell("TicketStock cannot have a negative quantity");
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
  private static Behavior<Message> outOfStock(ActorContext<Message> context, int ticketId) {
    return Behaviors.receive(Message.class)
      .onMessage(ProcessOrder.class, message -> {
        message.sender.tell("Ticket is out of stock");
        return Behaviors.same();
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Message {}

  public static final class ProcessOrder implements Message {
    public final int ticketId;
    public final int userId;
    public final int quantityDecrementedBy;
    public final ActorRef<Object> sender;

    public ProcessOrder(int ticketId, int userId, int quantityDecrementedBy, ActorRef<Object> sender) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantityDecrementedBy = quantityDecrementedBy;
      this.sender = sender;
    }
  }
}
