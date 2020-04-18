package org.mvrck.training.actor;

/********************************************************************************
 * Only actor message types, in order to avoid circular package dependencies.
 *
 * If we put both behavior and messages inside actor.GuardianActor, dependencies will be:
 *   http  ------> actor.*
 *   http  <------ actor.GuardianActor //Guardian holds http Route
 * which is not good, as it is circular.
 *
 * By splitting the behavior and message types, the package dependencies are:
 *   main.GuardianBehavior ---------------> actor.GuardianActor.Message
 *   main.GuardianBehavior ----> http
 *                               http ----> actor
 * So, we avoid the circular dependencies.
 *
 * Not so good to split the behavior and message into two classes, but not as bad
 * as a circular package dependency.
 ********************************************************************************/
public class GuardianActor {
  // Actor behaviors are in GuardianBehavior

  /********************************************************************************
   * Actor Messages
   ********************************************************************************/
  public interface Message {}
  public static class TerminateHttp implements Message {}
}
