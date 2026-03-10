import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.{ForeachWriter, Row}

import scala.collection.mutable

class KafkaSink(topic: String, servers: String) extends ForeachWriter[Row] {
  val kafkaProperties = new Properties
  kafkaProperties.put("bootstrap.servers", servers)
  kafkaProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProperties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")

  val results = new mutable.HashMap[String, Array[Byte]]

  var producer: KafkaProducer[String, Array[Byte]] = _

  override def open(partitionId: Long, version: Long): Boolean = {
    producer = new KafkaProducer(kafkaProperties)
  }

  override def process(value: Row): Unit = {
    producer.send(new ProducerRecord(topic, value.getAs[String](""), value.getAs[Array[Byte]]("")))
  }

  override def close(errorOrNull: Throwable): Unit = {
    producer.close()
  }
}
