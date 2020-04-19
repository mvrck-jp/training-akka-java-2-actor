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
        message.sender.tell(new GetOrderResponse(ticketId, userId, quantity));
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
    public final ActorRef<Response> sender;

    public GetOrder(int ticketId, int userId, ActorRef<Response> sender) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.sender = sender;
    }
  }

  /********************************************************************************
   *  Actor Response
   *******************************************************************************/
  public interface Response {}

  public static final class ErrorResponse implements Response {
    public final String errorMessage;

    public ErrorResponse(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public static final class GetOrderResponse implements Response {
    public final int ticketId;
    public final int userId;
    public final int quantity;

    public GetOrderResponse(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId ;
      this.quantity = quantity;
    }
  }
}
