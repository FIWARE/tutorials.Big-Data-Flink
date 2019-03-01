package org.fiware.cosmos.orion.flink.connector.examples.example2


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._

/**
  * Example2 Orion Connector
  * @author @sonsoleslp
  */
object Example2 {
  final val URL_CB = "http://localhost:1026/v2/entities/"
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.POST

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(entity => {
        val temp = entity.attrs("temperature").value.asInstanceOf[Number].floatValue()
        new Temp_Node(
          entity.id,
          temp)
      })
      .keyBy("id")
      .timeWindow(Time.seconds(5), Time.seconds(2))
      .min("temperature")
      .map(tempNode => {
        val url = URL_CB + tempNode.id + "/attrs"
        OrionSinkObject(tempNode.toString, url, CONTENT_TYPE, METHOD)
      })


    // Add Orion Sink
    OrionSink.addSink( processedDataStream )

    // print the results with a single thread, rather than in parallel
    processedDataStream.map(orionSinkObject => orionSinkObject.content).print().setParallelism(1)
    env.execute("Socket Window NgsiEvent")
  }

  case class Temp_Node(id: String, temperature: Float) extends  Serializable {
    override def toString :String = { "{\"temperature_min\": { \"value\":" + temperature + ", \"type\": \"Float\"}}" }
  }
}