// tag::cloud-connect[]
// tag::imports[]
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.query.*;

import java.time.Duration;
// end::imports[]

public class StartUsingCapella {
  // tag::connect-info[]
  // Update these variables to point to your Couchbase Capella instance and credentials.
  static String connectionString = "couchbases://cb.<your-endpoint-here>.cloud.couchbase.com";
  static String username = "username";
  static String password = "Password!123";
  static String bucketName = "travel-sample";
  // end::connect-info[]

  public static void main(String... args) {
    // tag::connect-env[]
    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password).environment(env -> {
          // Sets a pre-configured profile called "wan-development" to help avoid
          // latency issues when accessing Capella from a different Wide Area Network
          // or Availability Zone (e.g. your laptop).
          env.applyProfile("wan-development");
        })
    );
    // end::connect-env[]

    // tag::bucket[]
    // Get a bucket reference
    Bucket bucket = cluster.bucket(bucketName);
    bucket.waitUntilReady(Duration.ofSeconds(10));
    // end::bucket[]

    // tag::collection[]
    // Get a user-defined collection reference
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
// end::cloud-connect[]
