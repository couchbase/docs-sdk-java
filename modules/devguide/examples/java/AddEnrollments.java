import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.ClusterOptions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddEnrollments {

    public static void main(String[] args) {

        String connectionString = "<<connection-string>>"; // Replace this with Connection String
        String username = "<<username>>"; // Replace this with username from cluster access credentials
        String password = "<<password>>"; // Replace this with password from cluster access credentials

        Cluster cluster = Cluster.connect(connectionString, ClusterOptions.clusterOptions(username, password)
                    .environment(env -> env.applyProfile("wan-development"))
        );

        // Retrieves the student bucket you set up.
        Bucket bucket = cluster.bucket("student-bucket");

        // Forces the application to wait until the bucket is ready.
        bucket.waitUntilReady(Duration.ofSeconds(10));  

        // Retrieves the `art-school-scope` collection from the scope.
        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");  

        // Retrieves Hilary's student record, the `graphic design` course record, and the `art history` course record. 
        // Each method uses a SQL++ call to retrieve a single record from each collection.
        JsonObject hilary = retrieveStudent(cluster,"Hilary Smith"); 
        JsonObject graphic_design = retrieveCourse(cluster, "graphic design"); 
        JsonObject art_history = retrieveCourse(cluster, "art history");  

        // Couchbase does not have a native date type, so the common practice is to store dates as strings.
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);    

        // Stores the `enrollments` inside the student record as an array. 
        // `JsonArray.create()` creates an empty list structure.
        JsonArray enrollments = JsonArray.create();    

        // Adds two JSON elements to the `enrollments` array: the course that the enrollment relates to, and the date that the student enrolled in the course. 
        // To avoid repeating data all over the cluster, you store a reference to the course instead of the entire course record itself in this field. 
        // This means that you do not have to search through every single record if the course details change.
        enrollments.add(JsonObject.create()
                .put("course-id", graphic_design.getString("id"))
                .put("date-enrolled", currentDate));    
        enrollments.add(JsonObject.create()
                .put("course-id", art_history.getString("id"))
                .put("date-enrolled", currentDate));   

        // Adds the `enrollments` array to Hilary's student record.
        hilary.put("enrollments", enrollments);   

        // Commits the changes to the collection. 
        // The `upsert` function call takes the key of the record you want to insert or update and the record itself as parameters. 
        // If the `upsert` call finds a document with a matching ID in the collection, it updates the document. 
        // If there is no matching ID, it creates a new document.
        student_records.upsert(hilary.getString("id"), hilary); 

        cluster.disconnect();

    }

    private static JsonObject retrieveStudent(Cluster cluster, String name) throws CouchbaseException {

        QueryOptions studentQueryOptions = QueryOptions.queryOptions();
        studentQueryOptions.parameters(JsonObject.create().put("name", name));
        studentQueryOptions.scanConsistency(QueryScanConsistency.REQUEST_PLUS);

        final QueryResult result = cluster.query("select META().id, src.* " +
                        "from `student-bucket`.`art-school-scope`.`student-record-collection` src " +
                        "where src.`name` = $name", studentQueryOptions);

        return result.rowsAsObject().get(0);

    }

    private static JsonObject retrieveCourse(Cluster cluster, String course) throws CouchbaseException {

        QueryOptions courseQueryOptions = QueryOptions.queryOptions();
        courseQueryOptions.parameters(JsonObject.create().put("courseName", course));
        courseQueryOptions.scanConsistency(QueryScanConsistency.REQUEST_PLUS);

        final QueryResult result = cluster.query("select META().id, crc.* " +
                        "from `student-bucket`.`art-school-scope`.`course-record-collection` crc " +
                        "where crc.`course-name` = $courseName", courseQueryOptions);

        return result.rowsAsObject().get(0);

    }
}