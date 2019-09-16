package org.fiware.cosmos.orion.flink.connector.tutorial.example2


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._


/**
  * Example2 Orion Connector
  * @author @Javierlj
  */
object Example2 {
  final val CONTENT_TYPE = ContentType.Plain
  final val METHOD = HTTPMethod.POST

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity => (entity.attrs("count").value == "1"))
      .map(entity => new Sensor(entity.id))
      .keyBy("id")

      .timeWindow(Time.seconds(5), Time.seconds(2))
      .min("id")

    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)

    val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject("urn:ngsi-ld:Lamp" + node.id.takeRight(3) + "@on", "http://localhost:3001/iot/lamp" + node.id.takeRight(3), CONTENT_TYPE, METHOD)
    })
    OrionSink.addSink(sinkStream)
    env.execute("Socket Window NgsiEvent")
  }

  case class Sensor(id: String)
}