
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.libs.Akka
import akka.actor.Props
import cc.mewa.channels.ChannelManagerActor
import play.api.Play
import actors.ConnectionManagerActor


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val authtUrl = Play.current.configuration.getString("auth.url")
    val channelManager = Akka.system.actorOf(ChannelManagerActor.props(authtUrl), "channel-manager")
    val connectionManager = Akka.system.actorOf(Props[ConnectionManagerActor], "connection-manager")
  }
}