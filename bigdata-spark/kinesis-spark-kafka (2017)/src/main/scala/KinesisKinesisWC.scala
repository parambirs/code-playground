import java.nio.ByteBuffer
import java.util

//import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
//import cakesolutions.kafka.KafkaProducer.Conf
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.kinesis._
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.storage.StorageLevel

object KinesisKinesisWC {
  def main(args: Array[String]): Unit = {
    val appName = "KinesisSparkKinesis"
    val inputStreamName = "bet"
    val outputStreamName = "dsbet"
    val endpointUrl = "http://localhost:4567"

    // Configure Kinesis Client:
    val kinesisClient = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain)
    kinesisClient.setEndpoint(endpointUrl)

    val numShards = kinesisClient.describeStream(inputStreamName).getStreamDescription.getShards.size
    println(s"Number of shards: $numShards")

    // 1 Kinesis DStream for each shard
    val numStreams = numShards

    // Spark streaming batch interval
    val batchInterval = Milliseconds(2000)

    // Kinesis checkpoint interval is the interval at which the DynamoDB is updated with information
    // on sequence number of records that have been received.
    val kinesischeckpointInterval = batchInterval

    val regionName = "ap-southeast-2"

    val sparkConfig = new SparkConf().setMaster("local[4]").setAppName(appName)
    val ssc = new StreamingContext(sparkConfig, batchInterval)

    // Create the Kinesis DStreams
    val kinesisStreams = (0 until numStreams).map { i =>
      KinesisUtils.createStream(ssc, appName, inputStreamName, endpointUrl, regionName,
        InitialPositionInStream.TRIM_HORIZON, kinesischeckpointInterval, StorageLevel.MEMORY_AND_DISK_2)
    }

    // Union all the streams
    val unionStreams = ssc.union(kinesisStreams)

    // Convert each line of Array[Byte] to String, and split into words
    val words = unionStreams.flatMap(byteArray => new String(byteArray).split(" "))

    val wordCounts = words.map(word => (word, 1)).reduceByKey(_ + _)

    // Print the first 10 wordCounts
    wordCounts.print()

    ///////////////////// Write stuff to Kafka //////////////////////////////////

//    wordCounts.foreachRDD(rdd => {
//      rdd.foreachPartition(partitionOfRecords => {
//        val producer = KafkaProducer(Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = "localhost:9092"))
//        partitionOfRecords.foreach { record =>
//          producer.send(KafkaProducerRecord("test", record.toString()))
//        }
//      })
//    })

    ///////////////////// Write stuff to Kinesis //////////////////////////////////
    wordCounts.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        val kinesisClient = {
          val kinesisClient = new AmazonKinesisClient()
          kinesisClient.setEndpoint(endpointUrl)
          kinesisClient
        }

        var putRecordsRequestsCollection = new java.util.ArrayList[PutRecordsRequestEntry]
        partitionOfRecords.foreach(row => {
          val partitionKey = java.util.UUID.randomUUID.toString
          val putRecordsRequestEntry = new PutRecordsRequestEntry()
            .withPartitionKey(partitionKey)
            .withData(ByteBuffer.wrap(row.toString().getBytes("UTF-8")))
          putRecordsRequestsCollection.add(putRecordsRequestEntry)
          if (putRecordsRequestsCollection.size() > 499) {
            val putRecordsRequests = new PutRecordsRequest().withStreamName(outputStreamName)
              .withRecords(putRecordsRequestsCollection)
            kinesisClient.putRecords(putRecordsRequests)
            putRecordsRequestsCollection = new java.util.ArrayList[PutRecordsRequestEntry]
          }
        })

        if (putRecordsRequestsCollection.size() > 0) {
          val putRecordsRequests = new PutRecordsRequest().withStreamName(outputStreamName)
            .withRecords(putRecordsRequestsCollection)
          kinesisClient.putRecords(putRecordsRequests)
        }
      })
    })

    // Start the streaming context and await termination
    ssc.start()
    ssc.awaitTermination()
  }
}
