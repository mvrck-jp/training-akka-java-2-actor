package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class TicketStockParentActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Message> create(ActorRef<OrderParentActor.Message> orderParent){
    Map<Integer, ActorRef<TicketStockActor.Message>> children = new HashMap<>();
    return Behaviors.setup(context -> behavior(context, orderParent, children));
  }

  // private: never accessed from outside the actor
  private static Behavior<Message> behavior(
    ActorContext<Message> context,
    ActorRef<OrderParentActor.Message> orderParent,
    Map<Integer, ActorRef<TicketStockActor.Message>> children) {

    return Behaviors.receive(Message.class)
      .onMessage(CreateTicketStock.class, message -> {
        var child = context.spawn(TicketStockActor.create(orderParent, message.ticketId, message.quantity), Integer.toString(message.ticketId));
        children.put(message.ticketId, child);
        return Behaviors.same();
      })
      .onMessage(ProcessOrder.class, message -> {
        var child = children.get(message.ticketId);
        if(child == null) {
          System.out.println("bah");
        } else {
          child.tell(new TicketStockActor.ProcessOrder(message.ticketId, message.userId, message.quantity, message.sender));
        }
        return Behaviors.same();
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Message {}

  public static final class CreateTicketStock implements Message {
    public final int ticketId;
    public final int quantity;

    public CreateTicketStock(int ticketId, int quantity) {
      this.ticketId = ticketId;
      this.quantity = quantity;
    }
  }

  public static final class ProcessOrder implements Message {
    public final int ticketId;
    public final int userId;
    public final int quantity;
    public final ActorRef<OrderActor.Response> sender;

    public ProcessOrder(int ticketId, int userId, int quantity, ActorRef<OrderActor.Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.quantity = quantity;
      this.sender = sender;
    }
  }
}
