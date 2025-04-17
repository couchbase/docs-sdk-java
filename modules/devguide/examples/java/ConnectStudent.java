import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.ClusterOptions;
import java.time.Duration;

public class ConnectStudent {

    public static void main(String[] args) {

        String connectionString = "<<connection-string>>"; // Replace this with Connection String
        String username = "<<username>>"; // Replace this with username from cluster access credentials
        String password = "<<password>>"; // Replace this with password from cluster access credentials
        
        //Connecting to the cluster
        Cluster cluster = Cluster.connect(connectionString, ClusterOptions.clusterOptions(username, password)
        // Use the pre-configured profile below to avoid latency issues with your connection.
                    .environment(env -> env.applyProfile("wan-development"))
        );

        // The `cluster.bucket` retrieves the bucket you set up for the student cluster.
        Bucket bucket = cluster.bucket("student-bucket");  

        // Most of the Couchbase APIs are non-blocking. 
        // When you call one of them, your application carries on and continues to perform other actions while the function you called executes. 
        // When the function has finished executing, it sends a notification to the caller and the output of the call is processed.
        // While this usually works, in this code sample the application carries on without waiting for the bucket retrieval to complete after you make the call to `cluster.bucket`. 
        // This means that you now have to try to retrieve the scope from a bucket object that has not been fully initialized yet. 
        // To fix this, you can use the `waitUntilReady` call. 
        // This call forces the application to wait until the bucket object is ready.
        bucket.waitUntilReady(Duration.ofSeconds(10));  

        // The `bucket.scope` retrieves the `art-school-scope` from the bucket.
        Scope scope = bucket.scope("art-school-scope");   

        // The `scope.collection` retrieves the student collection from the scope.
        Collection student_records = scope.collection("student-record-collection"); 

        // A check to make sure the collection is connected and retrieved when you run the application. 
        // You can see the output using maven.
        System.out.println("The name of this collection is " + student_records.name()); 

        // Like with all database systems, it's good practice to disconnect from the Couchbase cluster after you have finished working with it.
        cluster.disconnect();
    }
}