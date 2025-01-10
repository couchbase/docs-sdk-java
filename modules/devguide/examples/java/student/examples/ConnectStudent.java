import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;

import java.time.Duration;

public class ConnectStudent {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");    // <1>

        Bucket bucket = cluster.bucket("student-bucket");    // <2>

        bucket.waitUntilReady(Duration.ofSeconds(10));    // <3>

        Scope scope = bucket.scope("art-school-scope");    //<4>

        Collection student_records = scope.collection("student-record-collection");    // <5>

        System.out.println("The name of this collection is " + student_records.name());    // <6>

        cluster.disconnect();    // <7>
    }
}
