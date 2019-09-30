import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.Collection;

import java.util.Optional;

class StartUsing {

    public static void main(String... args) {

// #tag::connect[]
Cluster cluster = Cluster.connect("localhost", "username", "password");
// #end::connect[]

// #tag::bucket[]
// get a bucket reference
Bucket bucket = cluster.bucket("bucket-name");
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
// #end::upsert-get[]

    }
}