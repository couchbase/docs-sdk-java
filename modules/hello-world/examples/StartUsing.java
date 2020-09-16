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

// #tag::imports[]
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.query.*;
// #end::imports[]

class StartUsing {

  static String connectionString = "localhost";
  static String username = "Administrator";
  static String password = "password";
  static String bucketName = "travel-sample";

  public static void local(String... args) {
    // #tag::connect_local[]
    Cluster cluster = Cluster.connect(connectionString, username, password);
    Bucket bucket = cluster.bucket(bucketName);
    Collection collection = bucket.defaultCollection();
    MutationResult upsertResult = collection.upsert(
        "my-document",
        JsonObject.create().put("name", "mike")
    );
    GetResult getResult = collection.get("my-document");
    String name = getResult.contentAsObject().getString("name");
    System.out.println(name); // name == "mike"
    QueryResult result = cluster.query("select \"Hello World\" as greeting");
    System.out.println(result.rowsAsObject());
    // #end::connect_local[]
  }

  public static void main(String... args) {
    // #tag::connect[]
    Cluster cluster = Cluster.connect(connectionString, username, password);
    // #end::connect[]

    // #tag::bucket[]
    // get a bucket reference
    Bucket bucket = cluster.bucket(bucketName);
    // #end::bucket[]

    // #tag::collection[]
    // get a collection reference
    Collection collection = bucket.defaultCollection();
    // #end::collection[]

    // #tag::upsert-get[]
    // Upsert Document
    MutationResult upsertResult = collection.upsert(
        "my-document",
        JsonObject.create().put("name", "mike")
    );

    // Get Document
    GetResult getResult = collection.get("my-document");
    String name = getResult.contentAsObject().getString("name");
    System.out.println(name); // name == "mike"
    // #end::upsert-get[]

    // #tag::n1ql-query[]
    QueryResult result = cluster.query("select \"Hello World\" as greeting");
    System.out.println(result.rowsAsObject());
    // #end::n1ql-query[]

  }

}
