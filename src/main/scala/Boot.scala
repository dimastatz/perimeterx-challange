import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object Boot {
  import system.dispatcher
  val searchService = new SearchService()
  implicit val system = ActorSystem("perimeterx-challenge")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    println("starting web server")
    val routes = routeDefault() ~ routeField() ~ routeSearch()
    val binding = Http().bindAndHandle(routes, "0.0.0.0", 8080)

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
      path(Segment / Segment) {
        (parameter1, parameter2) => {
          get {
            complete("search")
          }
        }
      }
    }
  }
}
