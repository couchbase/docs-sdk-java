import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;

public class buckets_and_clusters {

  String connectionString="localhost";
  String username="Administrator";
  String password="password";
  String bucketName="travel-sample";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;
  BucketSettings bucketSettings;

  private void init(){
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
    scope = bucket.defaultScope();
    collection = bucket.defaultCollection();
  }

  public void buckets_and_clusters_1() throws Exception {
    // #tag::buckets_and_clusters_1[]
    BucketManager manager = cluster.buckets();
    bucketSettings = BucketSettings.create("myBucket");
    manager.createBucket(bucketSettings);
    // #end::buckets_and_clusters_1[];
  }

  public static void main(String[] args) throws Exception{
    buckets_and_clusters obj = new buckets_and_clusters();
    obj.init();
    obj.buckets_and_clusters_1();
    System.out.println("Done.");
  }
}
