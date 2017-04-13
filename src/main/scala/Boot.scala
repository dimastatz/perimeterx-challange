import scala.util.Try
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object Boot {
  import system.dispatcher
  implicit val system = ActorSystem("perimeterx-challenge")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    // parse input parameters
    val host = Try(args(0)).getOrElse("192.168.99.100")
    val port = Try(args(1).toInt).getOrElse(9200)
    val index = Try(args(2)).getOrElse("page-views")
    val fields = Try(args(3).split(",").toList).
      getOrElse(List("ip", "domain", "blacklisted", "event_type"))

    // run web server
    val searchService = new SearchService(host, port, index, fields)
    val routes = routeDefault() ~ routeField(searchService)~ routeSearch(searchService)
    val binding = Http().bindAndHandle(routes, "0.0.0.0", 8080)

    println("web server started")

    sys addShutdownHook {
      //logger.debug(s"akka streams http server is shutting down")
      binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }
  }

  def routeDefault(): Route = get {
    pathEndOrSingleSlash {
      complete("Welcome to perimeterx-challange")
    }
  }

  def routeField(searchService: SearchService): Route = {
    pathPrefix("field") {
      parameters('type, 'value) {
        (t, v) => {
          get {
            complete(searchService.search(t,v))
          }
        }
      }
    }
  }

  def routeSearch(searchService: SearchService): Route = {
    pathPrefix("search") {
      parameters('value) {
        (v) => {
          get {
            complete(searchService.search(v))
          }
        }
      }
    }
  }
}
