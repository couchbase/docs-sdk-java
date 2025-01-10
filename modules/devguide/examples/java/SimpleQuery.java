import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;

// tag::class[]
public class SimpleQuery {
  // Update these variables to point to your Couchbase instance and credentials.
  static String connectionString = "localhost";
  static String username = "Administrator";
  static String password = "password";

  public static void main(String[] args) throws Exception {
    Cluster cluster = Cluster.connect(connectionString, username, password);
  
    {
      try {
        final QueryResult result = cluster.query("select * from `travel-sample`.inventory.airline limit 100",
          queryOptions().metrics(true));
  
        for (JsonObject row : result.rowsAsObject()) {
          System.out.println("Found row: " + row);
        }
  
        System.out.println("Reported execution time: " + result.metaData().metrics().get().executionTime());
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
    }
  }
}
// end::class[]