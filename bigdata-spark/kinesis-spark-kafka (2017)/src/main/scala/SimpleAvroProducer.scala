import java.util.Properties

import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object SimpleAvroProducer {
  val USER_SCHEMA =
    """
      |{
      |    "fields": [
      |        { "name": "str1", "type": "string" },
      |        { "name": "str2", "type": "string" },
      |        { "name": "int1", "type": "int" }
      |    ],
      |    "name": "myrecord",
      |    "type": "record"
      |}
    """.stripMargin

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")

    val parser = new Schema.Parser
    val schema = parser.parse(USER_SCHEMA)

    val recordInjection: Injection[GenericRecord, Array[Byte]] = GenericAvroCodecs.toBinary(schema)

    val producer = new KafkaProducer[String, Array[Byte]](props)

    for (i <- 0 until 1000) {
      val avroRecord = new Record(schema)
      avroRecord.put("str1", s"Str 1-$i")
      avroRecord.put("str2", s"Str 2-$i")
      avroRecord.put("int1", i)

      val bytes = recordInjection(avroRecord)

      val record = new ProducerRecord[String, Array[Byte]]("mytopic3", bytes)
      producer.send(record)

      Thread.sleep(250)
    }
  }
}
