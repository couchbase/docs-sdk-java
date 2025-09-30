/*
 * Copyright (c) 2021 Couchbase, Inc.
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

// tag::imports[]
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;
// tag::imports[]

public class SimpleVectorQuery {
  static String connectionString = "couchbases://cb.<your-endpoint-here>.cloud.couchbase.com";
  static String username = "Administrator";
  static String password = "password";

  public static void main(String[] args) throws Exception {
    Cluster cluster = Cluster.connect(
    connectionString,
    ClusterOptions.clusterOptions(username, password).environment(env -> {
    env.applyProfile("wan-development");
    })
    );

    { 
      try {
        // tag::hyperscale[]
        QueryResult result = cluster.query(
        "SELECT d.id, d.question, d.wanted_similar_color_from_search, " +
        "  ARRAY_CONCAT( " +
          "d.couchbase_search_query.knn[0].vector[0:4], " +
          "['...'] " +
        ") AS vector " +
        "FROM `vector-sample`.`color`.`rgb-questions` AS d " +
        "WHERE d.id = '#87CEEB';",
        queryOptions().metrics(true));

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println(row);
        }
        // end::hyperscale[]
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
    }
  }
}
