package org.mvrck.training.actor;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class OrderActor {
  /********************************************************************************
   *  Actor Behaviors
   *******************************************************************************/
  // public: the only Behavior factory method accessed from outside the actor
  public static Behavior<Void> create(int ticketId, int userId, int quantity){
    return Behaviors.setup(context -> behavior(ticketId, userId, quantity));
  }

  // private: never accessed from outside the actor
  private static Behavior<Void> behavior(int ticketId, int userId, int quantity) {
    return Behaviors.same();
  }
}
