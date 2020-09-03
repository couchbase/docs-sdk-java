import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

public class collections {

  String connectionString="localhost";
  String username="Administrator";
  String password="password";
  String bucketName="travel-sample";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;

  private void init(){
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
    scope = bucket.defaultScope();
    collection = bucket.defaultCollection();
  }

  public void collections_1() throws Exception {
    // #tag::collections_1[]
    collection = bucket.collection("flights"); // in default scope
    // #end::collections_1[];
  }

  public void collections_2() throws Exception {
    // #tag::collections_2[]
    collection = bucket.scope("marlowe_agency").collection("flights");
    // #end::collections_2[];
  }

  public static void main(String[] args) throws Exception{
    collections obj = new collections();
    obj.init();
    obj.collections_1();
    obj.collections_2();
    System.out.println("Done.");
  }
}
