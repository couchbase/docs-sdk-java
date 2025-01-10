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

import com.couchbase.client.core.env.CompressionConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

public class CompressionExample {

  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";

  Cluster cluster;

  private void init() {
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
  }

  public void compression_1() throws Exception {
    // tag::compression_1[]
    ClusterEnvironment env = ClusterEnvironment
        .builder()
        // start compressing at 1024 bytes
        .compressionConfig(CompressionConfig.minSize(1024))
        .build();
    // end::compression_1[]
  }

  public static void main(String[] args) throws Exception {
    CompressionExample obj = new CompressionExample();
    obj.init();
    obj.compression_1();
    System.out.println("Done.");
  }
}
