import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;

public class ScopedKvOperations {

    public static void main(String... args) {
        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

        // tag::upsert[]
        Bucket bucket = cluster.bucket("travel-sample");
        Scope inventoryScope = bucket.scope("inventory");

        Collection airportCollection = inventoryScope.collection("airport");

        {
            JsonObject content = JsonObject
            .create()
            .put("airportname", "Aeroporto D'Abruzzo")
            .put("country", "Italy");

            MutationResult result = airportCollection.upsert("airport-doc-key", content);
        }
        // end::upsert[]
    }

}
