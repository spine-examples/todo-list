# Kafka Storage

The [Apache Kafka](https://kafka.apache.org/)-based Spine Storage implementation.

_Disclaimer:_ the following document may contain large amount of Apache Kafka-specific terms. 
Please make sure to read the [official doc](https://kafka.apache.org/documentation/) before
diving in.

### Pre-requirements:
 - a copy of [the latest Kafka distribution](https://kafka.apache.org/downloads);
 - Kafka configuration files for:
   - Apache Zookeeper (e.g. the one found under the `config` dir of the Kafka distribution);
   - [Kafka broker(s)](https://kafka.apache.org/documentation/#brokerconfigs);
   - [Kafka producer](https://kafka.apache.org/documentation/#producerconfigs);
   - [Kafka consumer](https://kafka.apache.org/documentation/#consumerconfigs).
   
### Getting started

#### Entry point

To use Kafka Storage for your Spine application, create an instance of `KafkaStorageFactory` and 
pass it to your `BoundedContext` on creation.

The `KafkaStorageFactory` demands `Properties` for producer and consumer configurations. See 
[the doc](https://kafka.apache.org/documentation/) for the detailed description of the configs.
The required configurations are:
 - for Producer config:
```properties
key.serializer=io.spine.server.storage.kafka.MessageSerializer
value.serializer=io.spine.server.storage.kafka.MessageSerializer
```
It's also required to set the `group.id` value (`"0"` will work).
  - for Consumer config:
```properties
key.deserializer=io.spine.server.storage.kafka.MessageSerializer
value.deserializer=io.spine.server.storage.kafka.MessageSerializer
```
Also, it's recommended to set the following value for the Consumer config:
```properties
fetch.max.wait.ms=0
```
This turns off the Kafka "long poll" mechanism and dramatically speeds up read operations.

Also, `KafkaStorageFactory` requires two more parameters to initialize:
 - `consistencyLevel` - the **thread local** consistency, i.e. whether or not to block until 
 acknowledgement the execution after the write operations;
 - `maxPollAwait` - the maximum time the consumer waits to get the data fetched from the broker. 
 In practice, this parameter depends on the throughput of your Kafka cluster and network. For 
 a broker running locally, _100ms_ should be enough.
 
#### Broker configurations

These are the **required** Kafka broker configs:
```properties
log.retention.hours=-1
log.cleanup.policy=compact
```
These options are required to make sure all the data persists.

Please see [the broker config description](https://kafka.apache.org/documentation/#brokerconfigs) 
for the options required by Kafka itself.

#### Partition count

The broker configuration `num.partitions` is _very_ important for the Kafka Storage.
As the library creates Kafka topics dynamically, the configuration determines how many partitions 
will each topic have. The greater the number is, the more efficient the read operations can get.

Assume that you have a small domain model of e.g. 2 `Projection` types and 1 `Aggregate`. This 
would probably allow you to have over _10 000 partitions per topic_.

Generally speaking, you should make the number of partitions as big as possible. The limitations 
are:
 - some [Kafka internal issues](https://www.confluent.io/blog/how-to-choose-the-number-of-topicspartitions-in-a-kafka-cluster/)
 when syncing the data between the brokers;
 - the volume of the disk on the broker machines, as each partition (even empty) takes _21MB_ of 
 space for the metadata.
 
Also note that the partitions are created once the first record is written into the topic and it 
may take some time to create all of them.

The other option is to create the topics _manually_. For that, you need to create the topic with 
the same name as it would be done by the library (see `io.spine.server.storage.kafka.Topic`) and 
specify the number of partitions for that particular topic. This may be your case if you know that
there will be _many more_ or _many fewer_ records than the average in this particular topic 
(a topic typically represents a single type of a domain model).
