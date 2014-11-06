
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.libs.Akka
import akka.actor.Props
import cc.mewa.channels.ChannelManagerActor
import play.api.Play
import actors.ConnectionManagerActor
import cc.mewa.api.ChannelApp.AppEvent
import cc.mewa.channels.EventProxy


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val authtUrl = Play.current.configuration.getString("auth.url")
    val channelManager = Akka.system.actorOf(ChannelManagerActor.props(authtUrl), "channel-manager")
    val connectionManager = Akka.system.actorOf(Props[ConnectionManagerActor], "connection-manager")
    
    // Register eventProxy to send events to remote applications
    val eventProxy = Akka.system.actorOf(Props[EventProxy], "event-proxy")
    Akka.system.eventStream.subscribe(eventProxy, classOf[AppEvent])
    
  }
}