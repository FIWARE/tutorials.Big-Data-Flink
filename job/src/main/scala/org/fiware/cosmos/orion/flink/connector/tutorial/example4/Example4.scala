package org.fiware.cosmos.orion.flink.connector.examples.example4

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector.OrionSource

/**
  * Example4 Orion Connector
  * @author @sonsoleslp
  */
object Example4{

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
      .aggregate(new AverageAggregate)

    // print the results with a single thread, rather than in parallel

    processedDataStream.print().setParallelism(1)

    env.execute("Socket Window NgsiEvent")
  }

  case class Temp_Node(id: String, temperature: Float)

  class AverageAggregate extends AggregateFunction[Temp_Node, (Float,Float), Float] {
    override def createAccumulator() = (0L, 0L)

    override def add(value: (Temp_Node), accumulator: (Float, Float)) =
      (accumulator._1 + value.temperature, accumulator._2 + 1L)

    override def getResult(accumulator: (Float, Float)) = accumulator._1 / accumulator._2

    override def merge(a: (Float, Float), b: (Float, Float)) =
      (a._1 + b._1, a._2 + b._2)
  }


}