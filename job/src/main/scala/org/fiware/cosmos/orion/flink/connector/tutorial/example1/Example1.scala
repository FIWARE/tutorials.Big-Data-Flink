package org.fiware.cosmos.orion.flink.connector.examples.example1


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.{OrionSource}

/**
  * Example1 Orion Connector
  * @author @sonsoleslp
  */
object Example1{

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(entity => {
        val temp = entity.attrs("temperature").value.asInstanceOf[Number].floatValue()
        new Temp_Node( entity.id, temp)
      })
      .keyBy("id")
      .timeWindow(Time.seconds(5), Time.seconds(2))
      .min("temperature")

    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)
    env.execute("Socket Window NgsiEvent")
  }

  case class Temp_Node(id: String, temperature: Float)
}
