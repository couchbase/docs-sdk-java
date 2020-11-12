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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

public class CollectionsExample {

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

    // tag::collections_1[]
  public void collections_1() throws Exception {
    collection = bucket.collection("flights"); // in default scope
  }
    // end::collections_1[]

    // tag::collections_2[]
  public void collections_2() throws Exception {
    collection = bucket.scope("marlowe_agency").collection("flights");
  }
    // end::collections_2[]

  public static void main(String[] args) throws Exception {
    CollectionsExample obj = new CollectionsExample();
    obj.init();
    obj.collections_1();
    obj.collections_2();
    System.out.println("Done.");
  }
}
