package org.fiware.cosmos.orion.flink.connector.tutorial


import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.{OrionSource}

/**
  * Example1 Orion Connector
  * @author @sonsoleslp
  */
object Average{

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9000))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity=>entity.`type` == "Motion")
      .map(entity => new MotionSensor(entity.id, entity.attrs("count").value.toString().toInt))
      .keyBy("id")
      .timeWindow(Time.seconds(10))
      .sum(1)

    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)
    env.execute("Socket Window NgsiEvent")
  }

  case class MotionSensor(id: String, motion: Int)
}
