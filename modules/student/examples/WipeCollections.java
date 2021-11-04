import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;

import java.time.Duration;

/**
 * This class isn't part of the tutorial. It's just used
 * to wipe the collections between tests.
 */
public class WipeCollections {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        Bucket bucket = cluster.bucket("student-bucket");
        bucket.waitUntilReady(Duration.ofSeconds(10));

        Scope scope = bucket.scope("art-school-scope");
        Collection student_records = scope.collection("student-record-collection");
        Collection course_records = scope.collection("course-record-collection");

        try {

            try {
                student_records.remove("000001");
            }
            catch (DocumentNotFoundException e) {
                System.out.println("Student record not found");
            }

            try {
                course_records.remove("ART-HISTORY-000001");
            }
            catch (DocumentNotFoundException e) {
                System.out.println("Art history record not found");
            }

            try {
                course_records.remove("FINE-ART-000002");
            }
            catch (DocumentNotFoundException e) {
                System.out.println("Fine art record not found");
            }

            try {
                course_records.remove("GRAPHIC-DESIGN-000003");
            }
            catch (DocumentNotFoundException e) {
                System.out.println("Graphic design record not found");
            }

        }
        finally {
            cluster.disconnect();
        }
    }
}
