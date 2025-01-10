import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InsertStudent {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        Bucket bucket = cluster.bucket("student-bucket");
        bucket.waitUntilReady(Duration.ofSeconds(10));
        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");    // <.>

        JsonObject hilary = JsonObject.create()
                .put("name", "Hilary Smith")
                .put("date-of-birth",
                        LocalDate.of(1980, 12, 21)
                                .format(DateTimeFormatter.ISO_DATE));   // <.>


        student_records.upsert("000001", hilary);    // <.>

        cluster.disconnect();
    }
}
