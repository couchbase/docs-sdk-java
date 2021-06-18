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
    ClusterEnvironment env = ClusterEnvironment.builder()
        // [Customize client settings here]
        .build();

    // Create a cluster using the custom client settings.
    Cluster cluster = Cluster.connect(connectionString, ClusterOptions
        .clusterOptions(username, password)
        .environment(env));

    // [Your code to interact with the cluster]

    // Shut down gracefully.
    cluster.disconnect();
    env.shutdown();
    // end::client_settings_1[]
  }

  public void client_settings_2() throws Exception {
    // tag::client_settings_2[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .timeoutConfig(TimeoutConfig
            .kvTimeout(Duration.ofSeconds(5))
            .queryTimeout(Duration.ofSeconds(10)))
        .build();
    // end::client_settings_2[]
  }

  public void client_settings_3() throws Exception {
    // tag::client_settings_3[]
    ClusterEnvironment.Builder envBuilder = ClusterEnvironment.builder();
    envBuilder.timeoutConfig() // returns a TimeoutConfig.Builder
        .kvTimeout(Duration.ofSeconds(5))
        .queryTimeout(Duration.ofSeconds(10));
    ClusterEnvironment env = envBuilder.build();
    // end::client_settings_3[]
  }

  public void client_settings_4() throws Exception {
    // tag::client_settings_4[]
    System.setProperty("com.couchbase.env.timeout.kvTimeout", "10s"); // <1>
    System.setProperty("com.couchbase.env.timeout.queryTimeout", "15s");

    ClusterEnvironment environment = ClusterEnvironment.builder()
        .timeoutConfig(TimeoutConfig.kvTimeout(Duration.ofSeconds(5))) // <2>
        .build();
    // end::client_settings_4[]
  }

  public void client_settings_5() throws Exception {
    try {
      // tag::client_settings_5[]
      ClusterEnvironment env = ClusterEnvironment.builder()
          .securityConfig(SecurityConfig
              .enableTls(true)
          )
          .build();
      // end::client_settings_5[]
    } catch (InvalidArgumentException e) {
      System.err.println(e);
    }
  }

  public void client_settings_6() throws Exception {
    // tag::client_settings_6[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .ioConfig(IoConfig
            .networkResolution(NetworkResolution.AUTO)
        )
        .build();
    // end::client_settings_6[]
  }

  public void client_settings_7() throws Exception {
    // tag::client_settings_7[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .ioConfig(IoConfig.
            kvCircuitBreakerConfig(CircuitBreakerConfig.builder()
                .enabled(true)
                .volumeThreshold(45)
                .errorThresholdPercentage(25)
                .sleepWindow(Duration.ofSeconds(1))
                .rollingWindow(Duration.ofMinutes(2))
            ))
        .build();
    // end::client_settings_7[]
  }

  public void client_settings_8() throws Exception {
    // tag::client_settings_8[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .timeoutConfig(TimeoutConfig
            .kvTimeout(Duration.ofMillis(2500))
        )
        .build();
    // end::client_settings_8[]
  }

  public void client_settings_9() throws Exception {
    // tag::client_settings_9[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .compressionConfig(CompressionConfig.create().enable(true))
        .build();
    // end::client_settings_9[]
  }

  public void client_settings_10() throws Exception {
    // tag::client_settings_10[]
    ClusterEnvironment env = ClusterEnvironment.builder()
        .retryStrategy(BestEffortRetryStrategy.INSTANCE)

        .build();
    // end::client_settings_10[]
  }

  public static void main(String[] args) throws Exception {
    ClientSettingsExample obj = new ClientSettingsExample();
    obj.init();
    obj.client_settings_1();
    obj.client_settings_2();
    obj.client_settings_3();
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
