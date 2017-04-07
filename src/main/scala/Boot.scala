import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object Boot {
  import system.dispatcher
  implicit val system = ActorSystem("akka-example")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    println("starting web server")
    val routes = routeDefault() ~ routeField() ~ routeSearch()
    val binding = Http().bindAndHandle(routes, "0.0.0.0", 8080)

    //logger.debug(s"akka streams http server on 8080")
    StdIn.readLine()

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
      path(Segment / Segment) {
        (parameter1, parameter2) => {
          get {
            complete("field")
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
