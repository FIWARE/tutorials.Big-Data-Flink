package org.fiware.cosmos.orion.flink.connector.examples.example5

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.OrionSource

/**
  * Example5 Orion Connector
  * @author @sonsoleslp
  */
object Example5{

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = env.addSource(new OrionSource(9001))
    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(entity => {
        entity.attrs("information").value.asInstanceOf[Map[String, Any]]
      })
      .map(list => list("buses").asInstanceOf[List[Map[String,Any]]])
      .flatMap(bus => bus )
      .map(bus =>
        new Bus(bus("name").asInstanceOf[String], bus("price").asInstanceOf[ scala.math.BigInt].intValue()))
      .keyBy("name")
      .timeWindow(Time.seconds(5), Time.seconds(2))
      .min("price")

    // print the results with a single thread, rather than in parallel

    processedDataStream.print().setParallelism(1)

    env.execute("Socket Window NgsiEvent")
  }
  case class Bus(name: String,  price: Int)
}