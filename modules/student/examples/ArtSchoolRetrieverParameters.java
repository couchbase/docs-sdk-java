import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;

public class ArtSchoolRetrieverParameters {

    public static void main(String[] args) {
        Cluster cluster = Cluster.connect("localhost",
                "Administrator", "password");

        retrieveCourses(cluster);

        cluster.disconnect();
    }

    private static void retrieveCourses(Cluster cluster) {

        try {
            final QueryResult result = cluster.query("select crc.* " +
                            "from `student-bucket`.`art-school-scope`.`course-record-collection` crc " +
                            "where crc.`credit-points` < $creditPoints",    // <1>
                    QueryOptions.queryOptions()
                            .parameters(JsonObject.create().put("creditPoints", 200)));    // <2>

            for (JsonObject row : result.rowsAsObject()) {
                System.out.println("Found row: " + row);
            }

        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        }
    }
}
