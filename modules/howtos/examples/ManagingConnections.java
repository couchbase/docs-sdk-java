import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.AsyncCluster;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveBucket;
import com.couchbase.client.java.ReactiveCluster;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.nio.file.Paths;
import java.time.Duration;

public class ManagingConnections {
  public static void main(String... args) {

    {
      // #tag::simpleconnect[]
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
      // #end::simpleconnect[]
    }

    {
      // #tag::multinodeconnect[]
      Cluster cluster = Cluster.connect("192.168.56.101,192.168.56.102", "username", "password");
      // #end::multinodeconnect[]
    }


    {
      // #tag::customenv[]
      ClusterEnvironment env = ClusterEnvironment.builder()
          // Customize client settings by calling methods on the builder
          .build();

      // Create a cluster using the environment's custom client settings.
      Cluster cluster = Cluster.connect("127.0.0.1", ClusterOptions
          .clusterOptions("username", "password")
          .environment(env));

      // Shut down gracefully. Shut down the environment
      // after all associated clusters are disconnected.
      cluster.disconnect();
      env.shutdown();
      // #end::customenv[]
    }

    {
      // #tag::shareclusterenvironment[]
      ClusterEnvironment env = ClusterEnvironment.builder()
          .timeoutConfig(TimeoutConfig.kvTimeout(Duration.ofSeconds(5)))
          .build();

      Cluster clusterA = Cluster.connect(
          "clusterA.example.com",
          ClusterOptions.clusterOptions("username", "password")
              .environment(env));

      Cluster clusterB = Cluster.connect(
          "clusterB.example.com",
          ClusterOptions.clusterOptions("username", "password")
              .environment(env));

      // ...

      // For a graceful shutdown, disconnect from the clusters
      // AND shut down the custom environment when then program ends.
      clusterA.disconnect();
      clusterB.disconnect();
      env.shutdown();
      // #end::shareclusterenvironment[]
    }

    // todo use this example when beta 2 is released.
//    {
//      // #tag::seednodes[]
//      int customKvPort = 12345;
//      int customManagerPort = 23456;
//      Set<SeedNode> seedNodes = new HashSet<>(Arrays.asList(
//          SeedNode.create("127.0.0.1",
//              Optional.of(customKvPort),
//              Optional.of(customManagerPort))));
//

//      Cluster cluster = Cluster.connect(seedNodes, "username", "password");
//      // #end::customconnect[]
//    }

    {
      // #tag::connectionstringparams[]
      Cluster cluster = Cluster.connect(
          "127.0.0.1?io.maxHttpConnections=23&io.networkResolution=external", "username", "password");
      // #end::connectionstringparams[]
    }

    {
      // #tag::blockingtoasync[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      Bucket bucket = cluster.bucket("travel-sample");

      // Same API as Bucket, but completely async with CompletableFuture
      AsyncBucket asyncBucket = bucket.async();

      // Same API as Bucket, but completely reactive with Flux and Mono
      ReactiveBucket reactiveBucket = bucket.reactive();

      cluster.disconnect();
      // #end::blockingtoasync[]
    }

    {
      // #tag::reactivecluster[]
      ReactiveCluster cluster = ReactiveCluster.connect("127.0.0.1", "username", "password");
      ReactiveBucket bucket = cluster.bucket("travel-sample");

      // A reactive cluster's disconnect methods returns a Mono<Void>.
      // Nothing actually happens until you subscribe to the Mono.
      // The simplest way to subscribe is to await completion by calling call `block()`.
      cluster.disconnect().block();
      // #end::reactivecluster[]
    }

    {
      // #tag::asynccluster[]
      AsyncCluster cluster = AsyncCluster.connect("127.0.0.1", "username", "password");
      AsyncBucket bucket = cluster.bucket("travel-sample");

      // An async cluster's disconnect methods returns a CompletableFuture<Void>.
      // The disconnection starts as soon as you call disconnect().
      // The simplest way to wait for the disconnect to complete is to call `join()`.
      cluster.disconnect().join();
      // #end::asynccluster[]
    }


    {
      // #tag::tls[]
      ClusterEnvironment env = ClusterEnvironment.builder()
          .securityConfig(SecurityConfig.enableTls(true)
              .trustCertificate(Paths.get("/path/to/cluster.cert")))
          .build();
      // #end::tls[]
    }

    {
      // #tag::dnssrv[]
      ClusterEnvironment env = ClusterEnvironment.builder()
          .ioConfig(IoConfig.enableDnsSrv(true))
          .build();
      // #end::dnssrv[]
    }
  }
}
