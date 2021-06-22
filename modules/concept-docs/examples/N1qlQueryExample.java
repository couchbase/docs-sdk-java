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

import com.couchbase.client.core.error.IndexExistsException;
import com.couchbase.client.core.error.IndexNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.CreateQueryIndexOptions;
import com.couchbase.client.java.manager.query.QueryIndexManager;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class N1qlQueryExample {

  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";
  String bucketName = "travel-sample";
  String id = "user::" + UUID.randomUUID();

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

  public void n1ql_query_1() throws Exception {
    // tag::n1ql_query_1[]
    QueryResult result = cluster.query(
        "select count(*) from `travel-sample`.inventory.airport where country = ?",
        QueryOptions.queryOptions().adhoc(false).parameters(JsonArray.from("France"))
    );
    // end::n1ql_query_1[]
  }

  public void n1ql_query_2() throws Exception {
    try {
      setup_dropIndexes();
      // tag::n1ql_query_2[]
      QueryIndexManager indexManager = cluster.queryIndexes();

      indexManager.createPrimaryIndex(bucketName);
      indexManager.createIndex(bucketName, "ix_name", Collections.singletonList("name"));
      indexManager.createIndex(bucketName, "ix_email", Collections.singletonList("preferred_email"));
      // end::n1ql_query_2[]
    } catch (IndexExistsException e) {
      System.err.println(e);
    }
  }

  public void n1ql_query_3() throws Exception {
    try {
      setup_dropIndexes();
      // tag::n1ql_query_3[]
      QueryIndexManager indexManager = cluster.queryIndexes();

      indexManager.createPrimaryIndex(bucketName,
          CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().deferred(true));
      indexManager.createIndex(bucketName, "ix_name", Collections.singletonList("name"),
          CreateQueryIndexOptions.createQueryIndexOptions().deferred(true));
      indexManager.createIndex(bucketName, "ix_email", Collections.singletonList("preferred_email"),
          CreateQueryIndexOptions.createQueryIndexOptions().deferred(true));
      indexManager.buildDeferredIndexes(bucketName);
      indexManager.watchIndexes(bucketName, Arrays.asList("ix_name", "ix_email"), Duration.ofMinutes(5));
      // end::n1ql_query_3[]
    } catch (IndexExistsException e) {
      System.err.println(e);
    }
  }

  public void n1ql_query_4() throws Exception {
    // tag::n1ql_query_4[]
    String id = "user::" + UUID.randomUUID();
    collection.insert(
        id,
        JsonObject.create().put("value", true)
    );

    cluster.query(
        "select * from `" + bucketName + "`.inventory.airport where META().id = $id",
        QueryOptions.queryOptions()
            .parameters(JsonObject.create().put("id", id))
    );
    // end::n1ql_query_4[]
  }

  public void n1ql_query_5() throws Exception {
    // tag::n1ql_query_5[]
    cluster.query(
        "select * from `" + bucketName + "`.inventory.airport where META().id = $id",
        QueryOptions.queryOptions()
            .parameters(JsonObject.create().put("id", id))
            .scanConsistency(QueryScanConsistency.REQUEST_PLUS)
    );
    // end::n1ql_query_5[]
  }

  private void setup_dropIndexes() {
    try {
      QueryIndexManager indexManager = cluster.queryIndexes();
      indexManager.dropPrimaryIndex(bucketName);
      indexManager.dropIndex(bucketName, "ix_name");
      indexManager.dropIndex(bucketName, "ix_email");
    } catch (IndexNotFoundException e) {
      System.out.println("No index to drop, carrying on...");
    }
  }

  public static void main(String[] args) throws Exception {
    N1qlQueryExample obj = new N1qlQueryExample();
    obj.init();
    obj.n1ql_query_1();
    obj.n1ql_query_2();
    obj.n1ql_query_3();
    obj.n1ql_query_4();
    obj.n1ql_query_5();
    System.out.println("Done.");
  }
}
