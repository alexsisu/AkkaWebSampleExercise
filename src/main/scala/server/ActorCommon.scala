package org.chicagoscala.awse.server
import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.util.Logging

// Some "common" stuff shared by several actors and supervisors.

trait NamedActor extends Actor {
  val actorName: String
}

sealed trait ActorManagementMessage
case class GetActorFor(actorName: String) extends ActorManagementMessage
case class Register(actor: Actor)    extends ActorManagementMessage
case class Unregister(actor: Actor)  extends ActorManagementMessage

trait ActorSupervision extends Actor with Logging {
  
  protected def makeActor(actorName: String): Actor
  protected def getNamedActorFor(actorName: String): Option[Actor] =
    ActorRegistry.actorsFor(classOf[NamedActor]) find { actor => 
      actor.actorName == actorName
    }
  
  def handleManagementMessage: PartialFunction[Any,Unit] = {
    case GetActorFor(actorName) => reply(getOrMakeActorFor(actorName))

    case Register(actor) => 
      log.ifInfo("Registering actor: "+actor)
      link(actor)

    case Unregister(actor) => 
      log.ifInfo("Registering actor: "+actor)
      unlink(actor)
  }

  protected def getOrMakeActorFor(actorName: String) = getNamedActorFor(actorName) match {
    case Some(a) => a
    case None => 
      log.ifInfo("Creating new Actor for "+actorName)
      val actor = makeActor(actorName)
      actor.start
      this ! Register(actor)
      actor
  }
}

