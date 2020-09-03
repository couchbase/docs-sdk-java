// #tag::imports[]
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.query.*;
// #end::imports[]

class StartUsing {

    static String connectionString="localhost";
    static String username="Administrator";
    static String password="password";
    static String bucketName="travel-sample";

    public static void local(String... args) {
        // #tag::connect_local[]
        Cluster cluster = Cluster.connect(connectionString, username, password);
        Bucket bucket = cluster.bucket(bucketName);
        Collection collection = bucket.defaultCollection();
         MutationResult upsertResult = collection.upsert(
            "my-document",
            JsonObject.create().put("name", "mike")
        );
        GetResult getResult = collection.get("my-document");
        String name = getResult.contentAsObject().getString("name");
        System.out.println(name); // name == "mike"
        QueryResult result = cluster.query("select \"Hello World\" as greeting");
        System.out.println(result.rowsAsObject());
        // #end::connect_local[]
    }

    public static void main(String... args) {
        // #tag::connect[]
        Cluster cluster = Cluster.connect(connectionString, username, password);
        // #end::connect[]

        // #tag::bucket[]
        // get a bucket reference
        Bucket bucket = cluster.bucket(bucketName);
        // #end::bucket[]

        // #tag::collection[]
        // get a collection reference
        Collection collection = bucket.defaultCollection();
        // #end::collection[]

        // #tag::upsert-get[]
        // Upsert Document
        MutationResult upsertResult = collection.upsert(
            "my-document",
            JsonObject.create().put("name", "mike")
        );

        // Get Document
        GetResult getResult = collection.get("my-document");
        String name = getResult.contentAsObject().getString("name");
        System.out.println(name); // name == "mike"
        // #end::upsert-get[]

        // #tag::n1ql-query[]
        QueryResult result = cluster.query("select \"Hello World\" as greeting");
        System.out.println(result.rowsAsObject());
        // #end::n1ql-query[]

    }

}
