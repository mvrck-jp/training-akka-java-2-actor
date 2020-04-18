package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class OrderParentActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Message> create(){
    Map<String, ActorRef<OrderActor.Message>> children = new HashMap<>();
    return Behaviors.setup(context -> behavior(context, children));
  }

  // private: never accessed from outside the actor
  private static Behavior<Message> behavior(
    ActorContext<Message> context,
    Map<String, ActorRef<OrderActor.Message>> children) {

    return Behaviors.receive(Message.class)
      .onMessage(CreateOrder.class, message -> {
        var orderId = UUID.randomUUID();
        var child = context.spawn(OrderActor.create(message.ticketId, message.userId, message.quantity), orderId.toString());
        children.put(orderId.toString(), child);
        message.sender.tell("order was created");
        return behavior(context, children);
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Message {}

  public static final class CreateOrder implements Message {
    public final int ticketId;
    public final int userId;
    public final int quantity;
    public final ActorRef<Object> sender;

    public CreateOrder(int ticketId, int userId, int quantity, ActorRef<Object> sender) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.quantity = quantity;
      this.sender = sender;
    }
  }

}
