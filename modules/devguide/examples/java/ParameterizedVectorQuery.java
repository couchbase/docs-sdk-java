import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

public class ParameterizedVectorQuery {
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
// tag::parameterized[]
	final QueryResult result = cluster.query(
        "SELECT d.id, d.question, d.wanted_similar_color_from_search, " +
        "  ARRAY_CONCAT( " +
          "d.couchbase_search_query.knn[0].vector[0:4], " +
          "['...'] " +
        ") AS vector " +
	    "FROM `vector-sample`.`color`.`rgb-questions` AS d " +
		"WHERE d.id = $id;",
	queryOptions()
		.parameters(JsonObject.create().put("id", "#87CEEB")));

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println(row);
          }
// end::parameterized[]
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
    }
  }
}
