package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class OrderParentActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(){
    Map<String, ActorRef<OrderActor.Command>> children = new HashMap<>();
    return Behaviors.setup(context -> behavior(context, children));
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> behavior(
    ActorContext<Command> context,
    Map<String, ActorRef<OrderActor.Command>> children) {

    return Behaviors.receive(Command.class)
      .onMessage(CreateOrder.class, message -> {
        var orderId = UUID.randomUUID();
        var child = context.spawn(OrderActor.create(message.ticketId, message.userId, message.quantity), orderId.toString());
        children.put(orderId.toString(), child);
        child.tell(new OrderActor.GetOrder(message.ticketId, message.userId, message.sender));
        return behavior(context, children);
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Command {}

  public static final class CreateOrder implements Command {
    public final int ticketId;
    public final int userId;
    public final int quantity;
    public final ActorRef<OrderActor.Response> sender;

    public CreateOrder(int ticketId, int userId, int quantity, ActorRef<OrderActor.Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.quantity = quantity;
      this.sender = sender;
    }
  }

}
