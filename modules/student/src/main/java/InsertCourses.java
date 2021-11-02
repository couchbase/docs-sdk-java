import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;

import java.time.Duration;

public class InsertCourses {

    public static void main(String[] args) {
        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        Bucket bucket = cluster.bucket("student-bucket");
        bucket.waitUntilReady(Duration.ofSeconds(10));
        Scope scope = bucket.scope("art-school-scope");
        Collection course_records = scope.collection("course-record-collection");

        addCourse(course_records, "ART-HISTORY-000001", "art history", "fine art", 100);
        addCourse(course_records, "FINE-ART-000002", "fine art", "fine art", 50);
        addCourse(course_records, "GRAPHIC-DESIGN-000003", "graphic design", "media and communication", 200);

        cluster.disconnect();
    }

    private static void addCourse(Collection collection, String id, String name,
                                  String faculty, int creditPoints) {

        JsonObject course = JsonObject.create()
                .put("course-name", name)
                .put("faculty", faculty)
                .put("credit-points", creditPoints)
                .put("type", "course");

        collection.upsert(id, course);

    }
}
