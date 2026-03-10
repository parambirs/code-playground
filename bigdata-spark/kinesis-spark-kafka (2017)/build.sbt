name := "KinesisSparkKafka"

version := "1.0"

scalaVersion := "2.11.8"

val sparkVersion = "2.1.1"
//val kafkaVersion = "0.10.1.1"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  "Confluent Maven Repo" at "http://packages.confluent.io/maven/"
)

libraryDependencies ++= Seq(
  "org.apache.spark"  %%  "spark-core"                      % sparkVersion,
  "org.apache.spark"  %%  "spark-sql"                       % sparkVersion,
  "org.apache.spark"  %%  "spark-streaming"                 % sparkVersion,
  "org.apache.spark"  %%  "spark-streaming-kinesis-asl"     % sparkVersion,
  "org.apache.spark"  %   "spark-streaming-kafka-0-10_2.11" % sparkVersion,
  "org.apache.spark"  %% "spark-sql-kafka-0-10"             % sparkVersion,
//  "net.cakesolutions" %%  "scala-kafka-client"              % kafkaVersion,
//  "org.apache.kafka"  %   "kafka-clients"                   % "0.10.2.0",
  "org.apache.avro"   %   "avro"                            % "1.8.2",
  "com.twitter"       %%  "bijection-avro"                  % "0.9.5",
  "io.confluent"      % "kafka-avro-serializer"             % "3.2.1"
).map(_.exclude("org.apache.kafka", "kafka-clients"))

libraryDependencies += "org.apache.kafka"  %   "kafka-clients" % "0.10.2.0"