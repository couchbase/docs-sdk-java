import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.ClusterOptions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InsertStudent {

    public static void main(String[] args) {

        String connectionString = "<<connection-string>>"; // Replace this with Connection String
        String username = "<<username>>"; // Replace this with username from cluster access credentials
        String password = "<<password>>"; // Replace this with password from cluster access credentials
        
        Cluster cluster = Cluster.connect(connectionString, ClusterOptions.clusterOptions(username, password)
                    .environment(env -> env.applyProfile("wan-development"))
        );

        Bucket bucket = cluster.bucket("student-bucket");
        bucket.waitUntilReady(Duration.ofSeconds(10));
        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");    

        // This `JsonObject` class creates and populates the student record.
        JsonObject hilary = JsonObject.create()
                .put("name", "Hilary Smith")
                .put("date-of-birth",
                        LocalDate.of(1980, 12, 21)
                                .format(DateTimeFormatter.ISO_DATE));

        // The `upsert` function inserts or updates documents in a collection. 
        // The first parameter is a unique ID for the document, similar to a primary key used in a relational database system. 
        // If the `upsert` call finds a document with a matching ID in the collection, it updates the document. 
        // If there is no matching ID, it creates a new document.
        student_records.upsert("000001", hilary);  

        cluster.disconnect();
    }
}