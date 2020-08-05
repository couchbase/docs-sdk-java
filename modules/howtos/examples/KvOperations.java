// #tag::imports[]
import com.couchbase.client.core.error.*;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.kv.*;

import java.time.Duration;

import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.InsertOptions.insertOptions;
import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
// #end::imports[]

class KvOperations {

  public static void main(String... args) {

Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

Bucket bucket = cluster.bucket("bucket-name");
Scope scope = bucket.scope("scope-name");
Collection collection = scope.collection("collection-name");

JsonObject json = JsonObject.create()
  .put("title", "My Blog Post")
  .put("author", "mike");


// #tag::apis[]
AsyncCollection asyncCollection = collection.async();
ReactiveCollection reactiveCollection = collection.reactive();
// #end::apis[]

{
// #tag::upsert[]
JsonObject content = JsonObject.create().put("author", "mike");

MutationResult result = collection.upsert("document-key", content);
// #end::upsert[]
}

{
// #tag::insert[]
try {
  JsonObject content = JsonObject.create().put("title", "My Blog Post");
  MutationResult insertResult = collection.insert("document-key", content);
} catch (DocumentExistsException ex) {
  System.err.println("The document already exists!");
} catch (CouchbaseException ex) {
  System.err.println("Something else happened: " + ex);
}
// #end::insert[]
}

{
// #tag::get-simple[]
try {
  GetResult getResult = collection.get("document-key"); 
  String title = getResult.contentAsObject().getString("title");
  System.out.println(title); // title == "My Blog Post"
} catch (DocumentNotFoundException ex) {
  System.out.println("Document not found!");
}
// #end::get-simple[]
}

{
// #tag::get[]
GetResult found = collection.get("document-key");
JsonObject content = found.contentAsObject();
if (content.getString("author").equals("mike")) {
    // do something
} else {
    // do something else
}
// #end::get[]
}

{
// #tag::replace[]
collection.upsert("my-document", JsonObject.create().put("initial", true));

GetResult result = collection.get("my-document");
JsonObject content = result.contentAsObject();
content.put("modified", true).put("initial", false);
collection.replace("my-document", content, replaceOptions().cas(result.cas()));
// #end::replace[]
}

{
// #tag::replace-retry[]
String id = "my-document";
collection.upsert(id, JsonObject.create().put("initial", true));

GetResult found = collection.get(id);
JsonObject content = found.contentAsObject();
content.put("modified", true).put("initial", false);
while (true) {
    try {
        collection.replace(id, content, replaceOptions().cas(found.cas()));
        break; // if successful, break out of the retry loop
    } catch (CasMismatchException ex) {
        // don't do anything, we'll retry the loop
    }
}
// #end::replace-retry[]
}

{
// #tag::remove[]
try {
  collection.remove("my-document");
} catch (DocumentNotFoundException ex) {
  System.out.println("Document did not exist when trying to remove");
}
// #end::remove[]
}

{
// #tag::durability[]
collection.upsert(
  "my-document",
  JsonObject.create().put("doc", true),
  upsertOptions().durability(DurabilityLevel.MAJORITY)
);
// #end::durability[]
}

{
// #tag::durability-observed[]
collection.upsert(
  "my-document",
  JsonObject.create().put("doc", true),
  upsertOptions().durability(PersistTo.NONE, ReplicateTo.TWO)
);
// #end::durability-observed[]
}

{
// #tag::expiry-insert[]
MutationResult insertResult = collection.insert(
  "my-document",
  json,
  insertOptions().expiry(Duration.ofHours(2))
);
// #end::expiry-insert[]
}

{
// #tag::expiry-get[]
GetResult found = collection.get("my-document", getOptions().withExpiry(true));
System.out.println("Expiry of found doc: " + found.expiry());
// #end::expiry-get[]
}

{
// #tag::expiry-replace[]
GetResult found = collection.get("my-document", getOptions().withExpiry(true));

collection.replace(
  "my-document",
  json,
  replaceOptions().expiry(found.expiry().get())
);
// #end::expiry-replace[]
}

{
// #tag::expiry-touch[]
collection.getAndTouch("my-document", Duration.ofDays(1));
// #end::expiry-touch[]
}

  }

}
