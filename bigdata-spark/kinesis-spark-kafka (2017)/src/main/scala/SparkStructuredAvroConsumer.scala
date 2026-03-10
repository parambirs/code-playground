import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.spark.sql.{Encoder, SparkSession}
import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import org.apache.spark.sql.streaming.ProcessingTime

case class KafkaMessage(key: Array[Byte], value: Array[Byte],
                        topic: String, partition: String, offset: Long, timestamp: Timestamp)

object SparkStructuredAvroConsumer {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder
      .appName("Spark-Structured-Kafka-Avro")
      .master("local[*]")
      .config("spark.stopGracefullyOnShutdown", "tr‌​ue")
      .getOrCreate()


    val topics = Array("mytopic3")

    import spark.implicits._

    object MyDeserializerWrapper {
      val deser = new KafkaAvroDeserializer()
    }
    spark.udf.register("deserialize", (topic: String, bytes: Array[Byte]) =>
      MyDeserializerWrapper.deser.deserialize(topic, bytes).toString
      //      "Deserialized"
    )

    val kafkaDF = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "mytopic2")
      .option("startingOffsets", "earliest")
      .load()
      //      .as[KafkaMessage]
      .selectExpr("""deserialize("mytopic3", value) AS message""")


    //    val decodedDs  = kafkaDF.mapPartitions(decodeMessages)
    //      .select($"value".as[Array[Byte]])
    //      .map(d => {
    //        val rec = reader.read(null, avroDecoderFactory.binaryDecoder(d, null))
    //        rec.toString
    //        val deviceId = rec.get("int1").asInstanceOf[Int]
    //        val deviceName = rec.get("str1").asInstanceOf[org.apache.avro.util.Utf8].toString
    //        new MyMessage(deviceId, deviceName)
    //      })


    //      .map(msg => {
    //        val schema = new Schema.Parser().parse(SimpleAvroProducer.USER_SCHEMA)
    //        val recordInjection: Injection[GenericRecord, Array[Byte]] = GenericAvroCodecs.toBinary(schema)
    //        println(s"key: ${msg.key}, value: ${msg.value}")
    //        recordInjection.invert(msg.value).get.toString
    //      })

    //    val query = kafkaDF.select("*").writeStream
    //      .outputMode("append")
    //      .format("console")
    //      .start()


    val writer = new KafkaSink("mytopic2", "localhost:9092")

    val query = kafkaDF.select("*")
      .writeStream
      .foreach(writer)
      .outputMode("update")
      .trigger(ProcessingTime("5 seconds"))
      .start()

    query.awaitTermination()

    //    directKafkaStream.foreachRDD(rdd => {
    //      println("--- New RDD with " + rdd.partitions.length + " partitions and " + rdd.count() + " records.")
    //      rdd.foreach(avroRecord => {
    //        val parser = new Schema.Parser
    //        val schema = parser.parse(SimpleAvroProducer.USER_SCHEMA)
    //        val recordInjection: Injection[GenericRecord, Array[Byte]] = GenericAvroCodecs.toBinary(schema)
    //        val record = recordInjection.invert(avroRecord.value()).get
    //
    //        println(s"str1=${record.get("str1")}, str2=${record.get("str2")}, int=${record.get("int1")}")
    //      })
    //    })

  }
}
