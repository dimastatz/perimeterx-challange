import scala.util._
import org.elasticsearch.index.query._
import org.elasticsearch.action.search._
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

class SearchService(args: Array[String] = Array()) {
  private val index = Try(args(0)).getOrElse("page-views")
  private val address = Try(args(1)).getOrElse("localhost")
  private val ports = Try(args(2).split(",").map(_.toInt)).getOrElse(Array(9200,9300))
  private val fields = Try(args(3).split(",").toList)
    .getOrElse(List("ip", "domain", "blacklisted", "event_type"))

  private val client = new TransportClient()
    .addTransportAddress(new InetSocketTransportAddress(address, ports(0)))
    .addTransportAddress(new InetSocketTransportAddress(address, ports(1)))

  def search(fieldValue: String): String = {
     fields.par.map(i => search(i, fieldValue)).mkString(System.lineSeparator())
  }

  def search(fieldType: String, fieldValue: String): String = {
    Try(search(fieldType, fieldValue, client)) match {
      case Success(x) => x
      case Failure(x) => s"search failed $fieldType $fieldValue $index $address $ports $x"
    }
  }

  private def search(fieldType: String, fieldValue: String, client: TransportClient) = {
   val search =  client
      .prepareSearch(index)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(QueryBuilders.termsQuery(fieldType, fieldValue))
      .request()

    val response = client.search(search).actionGet()
    response.getHits.getHits.map(i => i.getSourceAsString).mkString(System.lineSeparator())
  }
}
