/*
 * Copyright (c) 2020 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.*;
import com.couchbase.client.java.codec.JsonSerializer;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.nio.file.Paths;
import java.time.Duration;

public class ManagingConnections {

  private static class MyCustomJsonSerializer implements JsonSerializer {
    @Override
    public byte[] serialize(Object o) {
      throw new UnsupportedOperationException("just an example");
    }

    @Override
    public <T> T deserialize(Class<T> aClass, byte[] bytes) {
      throw new UnsupportedOperationException("just an example");
    }
  }

  public static void main(String... args) {

    {
      // tag::simpleconnect[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      Bucket bucket = cluster.bucket("travel-sample");
      Collection collection = bucket.defaultCollection();

      // You can access multiple buckets using the same Cluster object.
      Bucket anotherBucket = cluster.bucket("beer-sample");

      // You can access collections other than the default
      // if your version of Couchbase Server supports this feature.
      Scope customerA = bucket.scope("customer-a");
      Collection widgets = customerA.collection("widgets");

      // For a graceful shutdown, disconnect from the cluster when the program ends.
      cluster.disconnect();
      // end::simpleconnect[]
    }

    {
      // tag::multinodeconnect[]
      Cluster cluster = Cluster.connect("192.168.56.101,192.168.56.102", "username", "password");
      // end::multinodeconnect[]
    }

    {
      // tag::customenv[]
      // Connect to a cluster using custom client settings.
      Cluster cluster = Cluster.connect(
          "127.0.0.1",
          ClusterOptions.clusterOptions("username", "password")
              .environment(env -> {
                // "env" is a `ClusterEnvironment.Builder`. Customize
                // client settings by calling builder methods.

                // For example, set the default JSON serializer.
                env.jsonSerializer(new MyCustomJsonSerializer());

                // For example, set the default SQL++ query timeout to 30 seconds.
                env.timeoutConfig(timeout -> timeout.queryTimeout(Duration.ofSeconds(30)));

                // Don't call env.build()! The SDK takes care of that.
              })
      );

      // Shut down gracefully.
      cluster.disconnect();
      // end::customenv[]
    }

    {
      // tag::shareclusterenvironment[]
      ClusterEnvironment sharedEnvironment = ClusterEnvironment.builder()
          .timeoutConfig(timeout -> timeout.kvTimeout(Duration.ofSeconds(5)))
          .build();

      Cluster clusterA = Cluster.connect(
          "clusterA.example.com",
          ClusterOptions.clusterOptions("username", "password")
              .environment(sharedEnvironment)
      );

      Cluster clusterB = Cluster.connect(
          "clusterB.example.com",
          ClusterOptions.clusterOptions("username", "password")
              .environment(sharedEnvironment)
      );

      // ...

      // For a graceful shutdown, disconnect from the clusters
      // AND shut down the custom environment when then program ends.
      clusterA.disconnect();
      clusterB.disconnect();
      sharedEnvironment.shutdown();
      // end::shareclusterenvironment[]
    }

    // todo use this example when beta 2 is released.
    // {
    // // tag::seednodes[]
    // int customKvPort = 12345;
    // int customManagerPort = 23456;
    // Set<SeedNode> seedNodes = new HashSet<>(Arrays.asList(
    // SeedNode.create("127.0.0.1",
    // Optional.of(customKvPort),
    // Optional.of(customManagerPort))));
    //

    // Cluster cluster = Cluster.connect(seedNodes, "username", "password");
    // // end::customconnect[]
    // }

    {
      // tag::connectionstringparams[]
      Cluster cluster = Cluster.connect("127.0.0.1?io.maxHttpConnections=23&io.networkResolution=external", "username",
          "password");
      // end::connectionstringparams[]
    }

    {
      // tag::blockingtoasync[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      Bucket bucket = cluster.bucket("travel-sample");

      // Same API as Bucket, but completely async with CompletableFuture
      AsyncBucket asyncBucket = bucket.async();

      // Same API as Bucket, but completely reactive with Flux and Mono
      ReactiveBucket reactiveBucket = bucket.reactive();

      cluster.disconnect();
      // end::blockingtoasync[]
    }

    {
      // tag::reactivecluster[]
      ReactiveCluster cluster = ReactiveCluster.connect("127.0.0.1", "username", "password");
      ReactiveBucket bucket = cluster.bucket("travel-sample");

      // A reactive cluster's disconnect methods returns a Mono<Void>.
      // Nothing actually happens until you subscribe to the Mono.
      // The simplest way to subscribe is to await completion by calling call
      // `block()`.
      cluster.disconnect().block();
      // end::reactivecluster[]
    }

    {
      // tag::asynccluster[]
      AsyncCluster cluster = AsyncCluster.connect("127.0.0.1", "username", "password");
      AsyncBucket bucket = cluster.bucket("travel-sample");

      // An async cluster's disconnect methods returns a CompletableFuture<Void>.
      // The disconnection starts as soon as you call disconnect().
      // The simplest way to wait for the disconnect to complete is to call `join()`.
      cluster.disconnect().join();
      // end::asynccluster[]
    }

    {
      // tag::tls[]
      Cluster cluster = Cluster.connect(
              "couchbases://<your-endpoint-or-ip-address>",
              ClusterOptions.clusterOptions("username", "password").environment(env -> {
                env.securityConfig(SecurityConfig.trustCertificate(Paths.get("/path/to/cluster.cert")));
              })
      );
      // end::tls[]
    }

    {
      // tag::dnssrv[]
      ClusterEnvironment env = ClusterEnvironment.builder().ioConfig(IoConfig.enableDnsSrv(true)).build();
      // end::dnssrv[]
    }
  }
}
