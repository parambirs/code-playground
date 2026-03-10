import java.util.UUID

import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object SparkAvroConsumer {
  def main(args: Array[String]): Unit = {
    val sparkConfig = new SparkConf()
      .setAppName("Kafka-Spark-Kafka-Avro")
      .setMaster("local[*]")
    val sc = new SparkContext(sparkConfig)
    val ssc = new StreamingContext(sc, Milliseconds(2000))

    val topics = Array("mytopic3")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[ByteArrayDeserializer],
      "group.id" -> UUID.randomUUID().toString
    )


    val directKafkaStream = KafkaUtils.createDirectStream(
      ssc,
      PreferConsistent,
      Subscribe[String, Array[Byte]](topics, kafkaParams)
    )

    directKafkaStream.foreachRDD(rdd => {
      println("--- New RDD with " + rdd.partitions.length + " partitions and " + rdd.count() + " records.")
      rdd.foreach(avroRecord => {
        val parser = new Schema.Parser
        val schema = parser.parse(SimpleAvroProducer.USER_SCHEMA)
        val recordInjection: Injection[GenericRecord, Array[Byte]] = GenericAvroCodecs.toBinary(schema)
        val record = recordInjection.invert(avroRecord.value()).get

        println(s"str1=${record.get("str1")}, str2=${record.get("str2")}, int=${record.get("int1")}")
      })
    })

    // Start the streaming context and await termination
    ssc.start()
    ssc.awaitTermination()
  }
}
