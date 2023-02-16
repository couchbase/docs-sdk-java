// tag::start-using[]
// tag::imports[]
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.query.*;

import java.time.Duration;
// end::imports[]

public class StartUsing {
  // tag::connect-info[]
  // Update these variables to point to your Couchbase Server instance and credentials.
  static String connectionString = "couchbase://127.0.0.1";
  static String username = "Administrator";
  static String password = "password";
  static String bucketName = "travel-sample";
  // end::connect-info[]

  public static void main(String... args) {
    // tag::connect-env[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password).environment(env -> {
          // Customize client settings by calling methods on the "env" variable.
        })
    );
    // end::connect-env[]

    // tag::bucket[]
    // get a bucket reference
    Bucket bucket = cluster.bucket(bucketName);
    bucket.waitUntilReady(Duration.ofSeconds(10));
    // end::bucket[]

    // tag::collection[]
    // get a user-defined collection reference
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
    // Call the query() method on the scope object and store the result.
    Scope inventoryScope = bucket.scope("inventory");
    QueryResult result = inventoryScope.query("SELECT * FROM airline WHERE id = 10;");

    // Return the result rows with the rowsAsObject() method and print to the terminal.
    System.out.println(result.rowsAsObject());
    // end::n1ql-query[]
  }
}
// end::start-using[]
