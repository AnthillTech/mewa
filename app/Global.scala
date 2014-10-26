
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.libs.Akka
import akka.actor.Props
import cc.mewa.channels.ChannelManagerActor
import play.api.Play
import actors.ConnectionManagerActor
import cc.mewa.app.EventLogger
import cc.mewa.api.ChannelApp.EventProcessor


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val authtUrl = Play.current.configuration.getString("auth.url")
    val channelManager = Akka.system.actorOf(ChannelManagerActor.props(authtUrl), "channel-manager")
    val connectionManager = Akka.system.actorOf(Props[ConnectionManagerActor], "connection-manager")
    
    // connect applications to event buss
    val eventLoggerPath = Play.current.configuration.getString("eventlog.path").getOrElse("./")
    val eventLogger = Akka.system.actorOf(Props(new EventLogger(eventLoggerPath)))
    Akka.system.eventStream.subscribe(eventLogger, classOf[EventProcessor])
  }
}