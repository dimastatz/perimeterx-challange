import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object Boot {
  import system.dispatcher
  // TODO: refactor to use config file
  val defaultFields = List("ip", "domain", "blacklisted", "event_type")
  val searchService = new SearchService("localhost", 9200, "page-views", defaultFields)
  implicit val system = ActorSystem("perimeterx-challenge")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val routes = routeDefault() ~ routeField() ~ routeSearch()
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

  def routeField(): Route = {
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

  def routeSearch(): Route = {
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
