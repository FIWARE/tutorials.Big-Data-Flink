package org.fiware.cosmos.orion.flink.connector.examples.example7

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.fiware.cosmos.orion.flink.connector._
import org.apache.flink.api.common.functions.AggregateFunction

/**
  * Example7 Orion Connector
  * @author @sonsoleslp
  */
object Example7 {

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
      .aggregate(new Average)



    // print the results with a single thread, rather than in parallel
    processedDataStream.print().setParallelism(1)
    env.execute("Socket Window NgsiEvent")
  }

  case class Temp_Node(id: String, temperature: Float)


  // the accumulator, which holds the state of the in-flight aggregate
  class AverageAccumulator {
    var count = 0
    var sum : Float = 0
  }

  // implementation of an aggregation function for an 'average'
  class Average extends AggregateFunction[Temp_Node, AverageAccumulator, Double] {
    override def createAccumulator = new AverageAccumulator

    override def merge(a: AverageAccumulator, b: AverageAccumulator): AverageAccumulator = {
      a.count += b.count
      a.sum += b.sum
      a
    }

    override def add(value: Temp_Node, acc: AverageAccumulator) = {
      acc.sum += value.temperature
      acc.count += 1
      acc
    }

    override def getResult(acc: AverageAccumulator): Double = acc.sum / acc.count.toDouble
  }



}
