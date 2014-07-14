
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.libs.Akka
import akka.actor.Props
import com.anthill.channels.ChannelManagerActor


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val channelManager = Akka.system.actorOf(Props[ChannelManagerActor], "channel-manager")
    Logger.info("Channel manager started: " + channelManager)
  }
}