package org.fiware.cosmos.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.{NGSILDSource}

/**
  * Logger Example NGSI-LD Orion Connector
  * @author @Javierlj
  */
object LoggerLD {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new NGSILDSource(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(entity => new Sensor(entity.`type`, 1))
      .keyBy("device")
      .timeWindow(Time.seconds(60))
      .sum(1)

    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)
    env.execute("Socket Window NgsiLDEvent")
  }
}
case class Sensor(device: String, sum: Int)