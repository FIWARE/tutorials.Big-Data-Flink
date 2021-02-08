package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._


/**
  * Feedback Example Orion Connector
  * @author @Javierlj
  */
object Feedback {
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val CONTENT = "{  \"on\": {      \"type\" : \"command\",      \"value\" : \"\"  }}"
  final val HEADERS = Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity => (entity.`type`== "Motion" && entity.attrs("count").value == "1"))
      .map(entity => new Sensor(entity.id))
      .keyBy("id")
      .timeWindow(Time.seconds(5), Time.seconds(2))
      .min("id")

    // print the results with a single thread, rather than in parallel
    processedDataStream.printToErr().setParallelism(1)

    val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject(CONTENT, "http://orion:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE, METHOD, HEADERS)
    })
    OrionSink.addSink(sinkStream)
    env.execute("Socket Window NgsiEvent")
  }

  case class Sensor(id: String)
}