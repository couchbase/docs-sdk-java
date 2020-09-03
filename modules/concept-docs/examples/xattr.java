import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.kv.LookupInMacro;
import com.couchbase.client.java.kv.LookupInSpec;

import java.util.Collections;

public class xattr {

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

  public void xattr_1() throws Exception {
    // #tag::xattr_1[]
    collection.lookupIn(
        "airport_1254",
        Collections.singletonList(
        LookupInSpec.get(LookupInMacro.EXPIRY_TIME).xattr())
    );
    // #end::xattr_1[];
  }

  public static void main(String[] args) throws Exception{
    xattr obj = new xattr();
    obj.init();
    obj.xattr_1();
    System.out.println("Done.");
  }
}
