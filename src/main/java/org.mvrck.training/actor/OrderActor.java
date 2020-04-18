package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class OrderActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Message> create(int ticketId, int userId, int quantity){
    return Behaviors.setup(context -> behavior(ticketId, userId, quantity));
  }

  // private: never accessed from outside the actor
  private static Behavior<Message> behavior(int ticketId, int userId, int quantity) {
    return Behaviors.receive(Message.class)
      .onMessage(GetOrder.class, message -> {
        message.sender.tell("order was created");
        return Behaviors.same();
      }).build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Message {}

  public static final class GetOrder implements Message {
    public final int ticketId;
    public final int userId;
    public final ActorRef<Object> sender;

    public GetOrder(int ticketId, int userId, ActorRef<Object> sender) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.sender = sender;
    }
  }
}
