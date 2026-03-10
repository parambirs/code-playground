import java.util

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model._

object KinesisDumpRecords {
  def main(args: Array[String]): Unit = {
//    val appName = "KinesisWordProducer"
//    val streamName = "bet"
    val streamName = "dsbet"
    val endpointUrl = "http://localhost:4567"
//    val recordsPerSecond = 100
//    val wordsPerRecord = 5

    val kinesisClient = new AmazonKinesisClient(new DefaultAWSCredentialsProviderChain)
    kinesisClient.setEndpoint(endpointUrl)

    val numShards = kinesisClient.describeStream(streamName).getStreamDescription.getShards.size

    for (i <- 0 until numShards) {
      val getShardIteratorRequest = new GetShardIteratorRequest()
      getShardIteratorRequest.setStreamName(streamName)
      getShardIteratorRequest.setShardIteratorType(ShardIteratorType.TRIM_HORIZON)
      getShardIteratorRequest.setShardId("shardId-00000000000" + i)
      val getShardIteratorResult = kinesisClient.getShardIterator(getShardIteratorRequest)
      //    println(getShardIteratorResult.getShardIterator)

      val getRecordsRequest = new GetRecordsRequest
      getRecordsRequest.setShardIterator(getShardIteratorResult.getShardIterator)
      val getRecordsResult: GetRecordsResult = kinesisClient.getRecords(getRecordsRequest)
      val outputRecords: util.List[Record] = getRecordsResult.getRecords

      for (i <- 0 until outputRecords.size()) {
        val record = outputRecords.get(i)
        println(s"Record# $i:")
        println(new String(record.getData.array()))
      }

//      println(outputRecords)
      println(s"Total records: ${outputRecords.size()}")
    }


  }

}
