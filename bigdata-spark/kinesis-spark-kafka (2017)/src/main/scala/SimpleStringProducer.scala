import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object SimpleStringProducer {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    for (i <- 0 until 1000) {
      val record = new ProducerRecord[String, String]("mytopic2", s"value-$i")
      producer.send(record)
      Thread.sleep(250)
    }

    producer.close()
  }
}
