import java.util

import com.google.gson.{Gson, GsonBuilder, JsonParser}

import scala.util._
import org.apache.http.HttpHost
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.RestClient

class SearchService(host: String, port:Int, index:String, fields: List[String]){
  val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
  val client: RestClient = RestClient.builder(new HttpHost(host, port, "http")).build()

  def search(fieldValue: String): String = {
     fields
       .par
       .map(i => search(i, fieldValue))
       .filter(i => i.trim != "")
       .mkString(System.lineSeparator())
  }

  def search(fieldType: String, fieldValue: String): String = {
    Try(search(fieldType, fieldValue, client)) match {
      case Success(x) => x
      case Failure(x) => s"search failed $fieldType $fieldValue $host $port $x"
    }
  }

  // TODO: refactor to work with proper json
  private def search(fieldType: String, fieldValue: String, client: RestClient) = {
    // construct query and execute
    val query = "{\"query\":{\"match\":{\"" + fieldType + "\":\"" + fieldValue + "\"}}}"

    val response = client.performRequest(
      "GET", s"/$index/_search",
      new util.Hashtable[String, String](),
      new StringEntity(query))

    val jsonString = gson.toJson(new JsonParser().parse(EntityUtils.toString(response.getEntity)))
    jsonString.substring(jsonString.indexOf('[') + 1, jsonString.indexOf(']'))
  }
}

