import scala.util._
import java.net.InetAddress
import org.elasticsearch.index.query._
import org.elasticsearch.action.search._
import org.elasticsearch.client.transport._
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient

class SearchService(args: Array[String] = Array()) {
  private val client = initializeClient()
  private val defaultFields = List("ip", "domain", "blacklisted", "event_type")
  private val fields = Try(args(3).split(",").toList).getOrElse(defaultFields)

  def search(fieldValue: String): String = {
     fields.par.map(i => search(i, fieldValue)).mkString(System.lineSeparator())
  }

  def search(fieldType: String, fieldValue: String): String = {
    Try(search(fieldType, fieldValue, client)) match {
      case Success(x) => x
      case Failure(x) => s"search failed $fieldType $fieldValue $args $x"
    }
  }

  private def search(fieldType: String, fieldValue: String, client: TransportClient) = {
    val search =  client
      .prepareSearch(Try(args(0)).getOrElse("page-views"))
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(QueryBuilders.termsQuery(fieldType, fieldValue))
      .request()

    val response = client.search(search).actionGet()
    response.getHits.getHits.map(i => i.getSourceAsString).mkString(System.lineSeparator())
  }

  private def initializeClient() = {
    val address = Try(args(1)).getOrElse("127.0.0.1")
    val port = Try(args(2).toInt).getOrElse(9300)

    val broker = new InetSocketTransportAddress(InetAddress.getByName(address), port)
    new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(broker)
  }
}

