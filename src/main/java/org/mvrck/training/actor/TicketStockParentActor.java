package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class TicketStockParentActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(ActorRef<OrderParentActor.Command> orderParent){
    Map<Integer, ActorRef<TicketStockActor.Command>> children = new HashMap<>();
    return Behaviors.setup(context -> behavior(context, orderParent, children));
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> behavior(
    ActorContext<Command> context,
    ActorRef<OrderParentActor.Command> orderParent,
    Map<Integer, ActorRef<TicketStockActor.Command>> children) {

    return Behaviors.receive(Command.class)
      .onMessage(CreateTicketStock.class, command -> {
        var child = context.spawn(TicketStockActor.create(orderParent, command.ticketId, command.quantity), Integer.toString(command.ticketId));
        children.put(command.ticketId, child);
        return Behaviors.same();
      })
      .onMessage(ProcessOrder.class, command -> {
        var child = children.get(command.ticketId);
        if(child == null) {
          command.sender.tell(new OrderActor.ErrorResponse("No ticket stock for " + command.ticketId));
        } else {
          child.tell(new TicketStockActor.ProcessOrder(command.ticketId, command.userId, command.quantity, command.sender));
        }
        return Behaviors.same();
      })
      .build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Command {}

  public static final class CreateTicketStock implements Command {
    public final int ticketId;
    public final int quantity;

    public CreateTicketStock(int ticketId, int quantity) {
      this.ticketId = ticketId;
      this.quantity = quantity;
    }
  }

  public static final class ProcessOrder implements Command {
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
