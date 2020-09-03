import com.couchbase.client.core.env.CompressionConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

public class compression {

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

  public void compression_1() throws Exception {
    // #tag::compression_1[]
    ClusterEnvironment env = ClusterEnvironment
      .builder()
      // start compressing at 1024 bytes
      .compressionConfig(CompressionConfig.minSize(1024))
      .build();
    // #end::compression_1[];
  }

  public static void main(String[] args) throws Exception{
    compression obj = new compression();
    obj.init();
    obj.compression_1();
    System.out.println("Done.");
  }
}
