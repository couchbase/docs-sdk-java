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

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddEnrollments {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");    // <1>

        Bucket bucket = cluster.bucket("student-bucket");    // <2>
        bucket.waitUntilReady(Duration.ofSeconds(10));    // <3>

        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");    // <4>

        // Retrieve the records
        JsonObject hilary = retrieveStudent(cluster,"Hilary Smith");    // <5>
        JsonObject graphic_design = retrieveCourse(cluster, "graphic design");    // <5>
        JsonObject art_history = retrieveCourse(cluster, "art history");    // <5>

        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);     // <6>

        // Create Hilary's enrollments

        JsonArray enrollments = JsonArray.create();     // <7>

        enrollments.add(JsonObject.create()
                .put("course-id", graphic_design.getString("id"))
                .put("date-enrolled", currentDate));    // <8>

        enrollments.add(JsonObject.create()
                .put("course-id", art_history.getString("id"))
                .put("date-enrolled", currentDate));    // <8>

        hilary.put("enrollments", enrollments);     // <9>

        student_records.upsert(hilary.getString("id"), hilary);    // <10>

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
