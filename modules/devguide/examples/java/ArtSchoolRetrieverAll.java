import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.ClusterOptions;

public class ArtSchoolRetriever {

    public static void main(String[] args) {
        
        String connectionString = "<<connection-string>>"; // Replace this with Connection String
        String username = "<<username>>"; // Replace this with username from cluster access credentials
        String password = "<<password>>"; // Replace this with password from cluster access credentials

        Cluster cluster = Cluster.connect(connectionString, ClusterOptions.clusterOptions(username, password)
                    .environment(env -> env.applyProfile("wan-development"))
        );

        retrieveCourses(cluster);

        cluster.disconnect();
    }

    private static void retrieveCourses(Cluster cluster) {

        try {
            final QueryResult result = cluster.query("select crc.* from `student-bucket`.`art-school-scope`.`course-record-collection` crc");

            for (JsonObject row : result.rowsAsObject()) {
                System.out.println("Found row: " + row);
            }

        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        }
    }
}