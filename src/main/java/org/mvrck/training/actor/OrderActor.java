package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class OrderActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Command> create(int ticketId, int userId, int quantity){
    return Behaviors.setup(context -> behavior(new State(ticketId, userId, quantity)));
  }

  // private: never accessed from outside the actor
  private static Behavior<Command> behavior(State state) {
    return Behaviors.receive(Command.class)
      .onMessage(GetOrder.class, command -> {
        command.sender.tell(new GetOrderResponse(state.ticketId, state.userId, state.quantity));
        return Behaviors.same();
      }).build();
  }

  /********************************************************************************
   *  Actor Messages
   *******************************************************************************/
  public interface Command {}

  public static final class GetOrder implements Command {
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

  /********************************************************************************
   * State
   *******************************************************************************/
  private static  class State {
    public int ticketId;
    public int userId;
    public int quantity;

    public State(int ticketId, int userId, int quantity) {
      this.ticketId = ticketId;
      this.userId = userId;
      this.quantity = quantity;
    }
  }
}
