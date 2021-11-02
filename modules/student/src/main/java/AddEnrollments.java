import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddEnrollments {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        Bucket bucket = cluster.bucket("student-bucket");
        bucket.waitUntilReady(Duration.ofSeconds(10));

        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");

        // Retrieve the records
        JsonObject hilary = retrieveStudent(cluster,"Hilary Smith");
        JsonObject graphic_design = retrieveCourse(cluster, "graphic design");
        JsonObject art_history = retrieveCourse(cluster, "art history");

        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // Create Hilary's enrolments

        JsonArray enrolments = JsonArray.create();

        enrolments.add(JsonObject.create().put("course-id", graphic_design.getString("id"))
                .put("date-enrolled", currentDate));

        enrolments.add(JsonObject.create().put("course-id", art_history.getString("id"))
                .put("date-enrolled", currentDate));

        hilary.put("enrolments", enrolments);

        student_records.upsert(hilary.getString("id"), hilary);

        cluster.disconnect();

    }

    private static JsonObject retrieveStudent(Cluster cluster, String name) throws CouchbaseException {

        final QueryResult result = cluster.query("select META().id, src.* " +
                        "from `student-bucket`.`art-school-scope`.`student-record-collection` src " +
                        "where src.`name` = $name",
                QueryOptions.queryOptions()
                        .parameters(JsonObject.create().put("name", name)));

        return result.rowsAsObject().get(0);

    }

    private static JsonObject retrieveCourse(Cluster cluster, String course) throws CouchbaseException {

        final QueryResult result = cluster.query("select META().id, crc.* " +
                "from `student-bucket`.`art-school-scope`.`course-record-collection` crc " +
                "where crc.`course-name` = $courseName",
                QueryOptions.queryOptions()
                        .parameters(JsonObject.create().put("courseName", course)));

        return result.rowsAsObject().get(0);

    }

}
