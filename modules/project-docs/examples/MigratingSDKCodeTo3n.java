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

import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import java.time.Duration;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;

public class MigratingSDKCodeTo3n {

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
    scope = bucket.defaultScope();
    collection = bucket.defaultCollection();
  }

  public void migrating_sdk_code_to_3_n_1() throws Exception { // file:
                                                               // project-docs/pages/migrating-sdk-code-to-3.n.adoc
                                                               // line: 23
    // tag::migrating_sdk_code_to_3_n_1[]
    GetResult getResult = collection.get("airline_10", getOptions().timeout(Duration.ofSeconds(3)));
    // end::migrating_sdk_code_to_3_n_1[]
  }

  public void migrating_sdk_code_to_3_n_2() throws Exception { // file:
                                                               // project-docs/pages/migrating-sdk-code-to-3.n.adoc
                                                               // line: 30
    // tag::migrating_sdk_code_to_3_n_2[]
    QueryResult queryResult = cluster.query("select 1=1", queryOptions().timeout(Duration.ofSeconds(3)));
    // end::migrating_sdk_code_to_3_n_2[]
  }

  public void migrating_sdk_code_to_3_n_11() throws Exception { // file:
                                                                // project-docs/pages/migrating-sdk-code-to-3.n.adoc
                                                                // line: 160
    // tag::migrating_sdk_code_to_3_n_11[]
    Cluster cluster = Cluster.connect(connectionString, username, password);

    Bucket bucket = cluster.bucket(bucketName);

    GetResult getResult = bucket.defaultCollection().get("airline_10");

    cluster.disconnect();
    // end::migrating_sdk_code_to_3_n_11[]
  }

  public void migrating_sdk_code_to_3_n_22() throws Exception { // file:
                                                                // project-docs/pages/migrating-sdk-code-to-3.n.adoc
                                                                // line: 492
    // tag::migrating_sdk_code_to_3_n_22[]
    QueryResult queryResult = cluster.query("select 1=");
    if (!queryResult.metaData().warnings().isEmpty()) {
      // errors contain [{"msg":"syntax error - at end of input","code":3000}]
    }
    // end::migrating_sdk_code_to_3_n_22[]
  }

  public static void main(String[] args) throws Exception {
    MigratingSDKCodeTo3n obj = new MigratingSDKCodeTo3n();
    obj.init();
    obj.migrating_sdk_code_to_3_n_1();
    obj.migrating_sdk_code_to_3_n_2();
    obj.migrating_sdk_code_to_3_n_11();
    obj.migrating_sdk_code_to_3_n_22();
  }
}
