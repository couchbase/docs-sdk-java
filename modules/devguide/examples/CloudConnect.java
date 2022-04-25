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

// tag::cloud-connect[]
// tag::imports[]
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.query.*;
// end::imports[]

// tag::capella-imports[]

// Required for Capella connection configuration
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.env.ClusterEnvironment;
// end::capella-imports[]


public class CloudConnect {
  // Update these variables to point to your Couchbase Capella instance and credentials.
  static String endpoint = "cb.njg8j7mwqnvwjqah.cloud.couchbase.com";
  static String bucketname = "travel-sample";
  static String username = "username";
  static String password = "Password!123";
  
  public static void main(String... args) {
    // tag::connect[]
    // Configure TLS
    SecurityConfig.Builder securityConfig = SecurityConfig
      .enableTls(true); // Enable transport security

    // Build the environment with the TLS config
    ClusterEnvironment env = ClusterEnvironment
      .builder()
      .securityConfig(securityConfig)
      .build();
      
    ClusterOptions options = ClusterOptions
      .clusterOptions(username, password)
      .environment(env);

    // Initialize the Connection
    Cluster cluster = Cluster.connect(endpoint, options);
    // end::connect[]
    
    // tag::bucket[]
    // get a bucket reference
    Bucket bucket = cluster.bucket(bucketname);
    bucket.waitUntilReady(Duration.parse("PT10S")) ;
    // end::bucket[]

    // tag::collection[]
    // get a user defined collection reference
    Scope scope = bucket.scope("tenant_agent_00");
    Collection collection = scope.collection("users");
    // end::collection[]

    // tag::upsert-get[]
    // Upsert Document
    MutationResult upsertResult = collection.upsert(
            "my-document",
            JsonObject.create().put("name", "mike")
    );

    // Get Document
    GetResult getResult = collection.get("my-document");
    String name = getResult.contentAsObject().getString("name");
    System.out.println(name); // name == "mike"
    // end::upsert-get[]

    // tag::n1ql-query[]
    // Call the query() method on the cluster object and store the result.
    QueryResult result = cluster.query("select \"Hello World\" as greeting");
    
    // Return the result rows with the rowsAsObject() method and print to the terminal.
    System.out.println(result.rowsAsObject());
    // end::n1ql-query[]
  }
}
// end::cloud-connect[]

