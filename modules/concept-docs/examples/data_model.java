import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.util.Map;

public class data_model {

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

  public void data_model_1() throws Exception {
    // #tag::data_model_1[]
    Map<String,String> myMap = collection.map("name",String.class);
    // #end::data_model_1[];
  }

  public static void main(String[] args) throws Exception{
    data_model obj = new data_model();
    obj.init();
    obj.data_model_1();
    System.out.println("Done.");
  }
}
