import java.util.UUID

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._

object KafkaKafkaWC {
  def main(args: Array[String]): Unit = {
    val sparkConfig = new SparkConf()
      .setAppName("Kafka-Spark-Kafka")
      //      .set("spark.executor.cores", "16")
      .setMaster("local[8]")
    val ssc = new StreamingContext(sparkConfig, Milliseconds(2000))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "SparkKafkaConsumer",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("mytopic2")

    val directKafkaStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    directKafkaStream
      .foreachRDD(rdd => {
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        println("--- New RDD with " + rdd.partitions.length + " partitions and " + rdd.count() + " records.")
        rdd.foreach(record => println(record.value()))

        // some time later, after outputs have completed
        directKafkaStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      })

    // Start the streaming context and await termination
    ssc.start()
    ssc.awaitTermination()
  }
}
