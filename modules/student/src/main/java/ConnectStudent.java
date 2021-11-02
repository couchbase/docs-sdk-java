import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;

import java.time.Duration;

public class ConnectStudent {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        Bucket bucket = cluster.bucket("student-bucket");

        bucket.waitUntilReady(Duration.ofSeconds(10));

        Scope scope = bucket.scope("art-school-scope");

        Collection student_records = scope.collection("student-record-collection");

        System.out.println("The name of this collection is " + student_records.name());



        cluster.disconnect();
    }
}
