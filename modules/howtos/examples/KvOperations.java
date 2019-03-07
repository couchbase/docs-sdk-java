// #tag::imports[]
import com.couchbase.client.core.error.CASMismatchException;
import com.couchbase.client.core.error.DocumentAlreadyExistsException;
import com.couchbase.client.core.error.DocumentDoesNotExistException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.AsyncCollection;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;
import com.couchbase.client.java.kv.UpsertOptions;

import java.time.Duration;
import java.util.Optional;

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
  .put("foo", "bar")
  .put("baz", "qux");


// #tag::apis[]
AsyncCollection asyncCollection = collection.async();
ReactiveCollection reactiveCollection = collection.reactive();
// #end::apis[]

{
// #tag::upsert[]
JsonObject content = JsonObject.create()
  .put("foo", "bar")
  .put("baz", "qux");

MutationResult result = collection.upsert("document-key", content);
// #end::upsert[]
}

// #tag::insert[]
try {
  JsonObject content = JsonObject.create().put("foo", "bar");
  MutationResult insertResult = collection.insert("document-key", content);
} catch (DocumentAlreadyExistsException ex) {
  System.err.println("The document already exists!");
} catch (Exception ex) {
  System.err.println("Something else happened: " + ex);
}
// #end::insert[]

{
// #tag::get-simple[]
Optional<GetResult> getResult = collection.get("document-key");
if (getResult.isPresent()) {
  System.out.println("Found document: " + getResult.get());
} else {
  System.out.println("Document not found!");
}
// #end::get-simple[]
}

// #tag::get[]
collection.get("document-key").ifPresent(found -> {
  JsonObject content = found.contentAsObject();
  if (content.getString("foo").equals("bar")) {
    // do something
  } else {
    // do something else
  }
});
// #end::get[]

{
// #tag::replace[]
collection.upsert("my-document", JsonObject.create().put("initial", true));

Optional<GetResult> result = collection.get("my-document");
result.ifPresent(found -> {
  JsonObject content = found.contentAsObject();
  content
    .put("modified", true)
    .put("initial", false);
  collection.replace("my-document", content, replaceOptions().cas(found.cas()));
});
// #end::replace[]
}

// #tag::replace-retry[]
String id = "my-document";
collection.upsert(id, JsonObject.create().put("initial", true));

Optional<GetResult> result = collection.get(id);
result.ifPresent(found -> {
  JsonObject content = found.contentAsObject();
  content.put("modified", true).put("initial", false);
  while (true) {
    try {
      collection.replace(id, content, replaceOptions().cas(found.cas()));
      break; // if successful, break out of the retry loop
    } catch (CASMismatchException ex) {
      // don't do anything, we'll retry the loop
    }
  }
});
// #end::replace-retry[]

// #tag::remove[]
try {
  collection.remove("my-document");
} catch (DocumentDoesNotExistException ex) {
  System.out.println("Document did not exist when trying to remove");
}
// #end::remove[]

// #tag::durability[]
collection.upsert(
  "my-document",
  JsonObject.create().put("doc", true),
  upsertOptions().withDurabilityLevel(DurabilityLevel.MAJORITY)
);
// #end::durability[]

// #tag::durability-observed[]
collection.upsert(
  "my-document",
  JsonObject.create().put("doc", true),
  upsertOptions().withDurability(PersistTo.NONE, ReplicateTo.TWO)
);
// #end::durability-observed[]

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
collection
  .get("my-document", getOptions().withExpiration(true))
  .ifPresent(found ->
    System.out.println("Expiration of found doc: " + found.expiration())
  );
// #end::expiry-get[]
}

{
// #tag::expiry-replace[]
GetResult found = collection.get(
  "my-document",
  getOptions().withExpiration(true)
).get();

collection.replace(
  "my-document",
  json,
  replaceOptions().expiry(found.expiration().get())
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