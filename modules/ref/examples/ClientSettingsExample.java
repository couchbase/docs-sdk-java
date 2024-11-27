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

import com.couchbase.client.core.endpoint.CircuitBreakerConfig;
import com.couchbase.client.core.env.CompressionConfig;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.InvalidArgumentException;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.time.Duration;

public class ClientSettingsExample {

  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";
  String bucketName = "travel-sample";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;

  private void init() {
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
    scope = bucket.scope("inventory");
    collection = scope.collection("airport");
  }

  public void client_settings_1() throws Exception {
    // tag::client_settings_1[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> {
              // "env" is a `ClusterEnvironment.Builder`. Customize
              // client settings by calling builder methods.

              // Don't call env.build()! The SDK takes care of that.
            })
    );    // end::client_settings_1[]
  }

  public void client_settings_2() throws Exception {
    // tag::client_settings_2[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env // ClusterEnvironment.Builder
                .timeoutConfig(timeout -> timeout // TimeoutConfig.Builder
                    .kvTimeout(Duration.ofSeconds(5))
                    .queryTimeout(Duration.ofSeconds(10))
                )
                .ioConfig(io -> io // IoConfig.Builder
                    .maxHttpConnections(64)
                )
            )
    );
    // end::client_settings_2[]
  }

  public void client_settings_connection_string() throws Exception {
    // tag::client_settings_connection_string[]
    Cluster cluster = Cluster.connect(
        "couchbase://127.0.0.1?timeout.kvTimeout=10s&timeout.queryTimeout=15s", // <1>
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .timeoutConfig(timeout -> timeout
                    .kvTimeout(Duration.ofSeconds(5)) // <2>
                )
            )
    );
    // end::client_settings_connection_string[]
  }

  public void client_settings_4() throws Exception {
    // tag::client_settings_4[]
    System.setProperty("com.couchbase.env.timeout.kvTimeout", "10s"); // <1>
    System.setProperty("com.couchbase.env.timeout.queryTimeout", "15s");

    Cluster cluster = Cluster.connect(
        "couchbase://127.0.0.1?timeout.queryTimeout=30s", // <2>
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .timeoutConfig(timeout -> timeout
                    .kvTimeout(Duration.ofSeconds(5)) // <2>
                )
            )
    );
    // end::client_settings_4[]
  }

  public void client_settings_5() throws Exception {
    // tag::client_settings_5[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .securityConfig(security -> security
                    .enableTls(true)
                )
            )
    );
    // end::client_settings_5[]
    cluster.disconnect();
  }

  public void client_settings_6() throws Exception {
    // tag::client_settings_6[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .ioConfig(io -> io
                    .networkResolution(NetworkResolution.AUTO)
                )
            )
    );
    // end::client_settings_6[]
  }

  public void client_settings_7() throws Exception {
    // tag::client_settings_7[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .ioConfig(io -> io.
                    kvCircuitBreakerConfig(kvBreaker -> kvBreaker
                        .enabled(true)
                        .volumeThreshold(45)
                        .errorThresholdPercentage(25)
                        .sleepWindow(Duration.ofSeconds(1))
                        .rollingWindow(Duration.ofMinutes(2))
                    )
                )
            )
    );
    // end::client_settings_7[]
  }

  public void client_settings_8() throws Exception {
    // tag::client_settings_8[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .timeoutConfig(timeout -> timeout
                    .kvTimeout(Duration.ofMillis(2500))
                )
            )
    );
    // end::client_settings_8[]
  }

  public void client_settings_9() throws Exception {
    // tag::client_settings_9[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .compressionConfig(compression -> compression
                    .minSize(32)
                )
            )
    );
    // end::client_settings_9[]
  }

  public void client_settings_10() throws Exception {
    // tag::client_settings_10[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env -> env
                .retryStrategy(BestEffortRetryStrategy.INSTANCE)
            )
    );
    // end::client_settings_10[]
  }

  public static void main(String[] args) throws Exception {
    ClientSettingsExample obj = new ClientSettingsExample();
    obj.init();
    obj.client_settings_1();
    obj.client_settings_2();
    obj.client_settings_4();
    obj.client_settings_5();
    obj.client_settings_6();
    obj.client_settings_7();
    obj.client_settings_8();
    obj.client_settings_9();
    obj.client_settings_10();
    System.out.println("Done.");
  }
}
