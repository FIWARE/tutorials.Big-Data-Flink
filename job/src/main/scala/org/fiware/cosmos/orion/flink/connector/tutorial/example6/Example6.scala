package org.fiware.cosmos.orion.flink.connector.examples.example6

import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._

/**
  * Example6 Orion Connector
  * @author @sonsoleslp
  */

object Example6{
  final val URL_CB = "http://138.4.7.110:3002/avg"
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
        println("Entity",entity.id, "Temperature", temp)
        new Temp_Node( entity.id, temp)
      })
      .timeWindowAll(Time.seconds(10) )
      .max("temperature")

    // print the results with a single thread, rather than in parallel
    processedDataStream.map(p=>println("Max: "+ p))
    val sinkStream = processedDataStream.map(node => {
      new OrionSinkObject("{\"avg\":" + node.temperature + "}",URL_CB,CONTENT_TYPE,METHOD)
    })
    OrionSink.addSink(sinkStream)
    env.execute("Socket Window NgsiEvent")

  }

  case class Temp_Node(id: String, temperature: Float)
}
