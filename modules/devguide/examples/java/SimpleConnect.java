// tag::simple-connect[]
// tag::imports[]
import com.couchbase.client.java.*;
// end::imports[]

public class SimpleConnect {
  static String connectionString = "couchbases://example.com";
  static String username = "username";
  static String password = "Password!123";
  static String bucketName = "travel-sample";

  public static void main(String... args) {
    // tag::connect-string[]
    // Alternatively, connect without customizing the cluster envionrment.
    Cluster cluster = Cluster.connect(connectionString, username, password);
    // end::connect-string[]
    cluster.disconnect();
  }
}
// end::simple-connect[]
