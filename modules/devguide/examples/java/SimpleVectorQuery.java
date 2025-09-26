
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Scope;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.query.ReactiveQueryResult;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.ClusterOptions;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

public class SimpleVectorQuery {
  static String connectionString = "couchbase://127.0.0.1";
  static String username = "Administrator";
  static String password = "fred123!";

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
        final QueryResult result = cluster.query(
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
// end::hyperscale[]
          }
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
    }
  }
}
