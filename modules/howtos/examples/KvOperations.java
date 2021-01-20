/*
 * Copyright (c) 2020 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// tag::imports[]
import com.couchbase.client.core.error.*;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.kv.*;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;

import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.InsertOptions.insertOptions;
import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
// end::imports[]

class KvOperations {

  public static void main(String... args) {

Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

Bucket bucket = cluster.bucket("default");
Scope scope = bucket.scope("_default");
Collection collection = scope.collection("_default");

JsonObject json = JsonObject.create()
  .put("title", "My Blog Post")
  .put("author", "mike");


// tag::apis[]
AsyncCollection asyncCollection = collection.async();
ReactiveCollection reactiveCollection = collection.reactive();
// end::apis[]

{
// tag::upsert[]
JsonObject content = JsonObject.create().put("author", "mike").put("title","My Blog Post 1");

MutationResult result = collection.upsert("document-key", content);
// end::upsert[]
}

{
// tag::insert[]
try {
  JsonObject content = JsonObject.create().put("title", "My Blog Post 2");
  MutationResult insertResult = collection.insert("document-key", content);
} catch (DocumentExistsException ex) {
  System.err.println("The document already exists!");
} catch (CouchbaseException ex) {
  System.err.println("Something else happened: " + ex);
}
// end::insert[]
}

{
// tag::get-simple[]
try {
  GetResult getResult = collection.get("document-key");
  String title = getResult.contentAsObject().getString("title");
  System.out.println(title); // title == "My Blog Post"
} catch (DocumentNotFoundException ex) {
  System.out.println("Document not found!");
}
// end::get-simple[]
}

{
// tag::get[]
GetResult found = collection.get("document-key");
JsonObject content = found.contentAsObject();
if (content.getString("author").equals("mike")) {
    // do something
} else {
    // do something else
}
// end::get[]
}

{
// tag::replace[]
collection.upsert("my-document", JsonObject.create().put("initial", true));

GetResult result = collection.get("my-document");
JsonObject content = result.contentAsObject();
content.put("modified", true).put("initial", false);
collection.replace("my-document", content, replaceOptions().cas(result.cas()));
// end::replace[]
}

{
// tag::replace-retry[]
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
// end::replace-retry[]
}

{
// tag::remove[]
try {
  collection.remove("my-document");
} catch (DocumentNotFoundException ex) {
  System.out.println("Document did not exist when trying to remove");
}
// end::remove[]
}

{
  try {
// tag::durability[]
    collection.upsert(
        "my-document",
        JsonObject.create()
            .put("doc",
                true),
        upsertOptions().durability(DurabilityLevel.MAJORITY)
    );
// end::durability[]
  } catch(DurabilityImpossibleException di) {
    System.out.println(di);
  }
}

{
  try {
// tag::durability-observed[]
    collection.upsert(
        "my-document",
        JsonObject.create()
            .put("doc",
                true),
        upsertOptions().durability(PersistTo.NONE,
            ReplicateTo.TWO)
    );
// end::durability-observed[]
  } catch(ReplicaNotConfiguredException rnc){
    System.out.println(rnc);
  }
}

{
// tag::expiry-insert[]
MutationResult insertResult = collection.insert(
  "my-document",
  json,
  insertOptions().expiry(Duration.ofHours(2))
);
// end::expiry-insert[]
}

{
// tag::expiry-insert-instant[]
MutationResult insertResult = collection.insert(
  "my-document",
  json,
  insertOptions().expiry(Instant.now().plus(Period.ofMonths(2)))
);
// end::expiry-insert-instant[]
}

{
// tag::expiry-get[]
GetResult found = collection.get("my-document", getOptions().withExpiry(true));
System.out.println("Expiry of found doc: " + found.expiry());
// end::expiry-get[]
}

{
// tag::expiry-replace[]
GetResult found = collection.get("my-document", getOptions().withExpiry(true));

collection.replace(
  "my-document",
  json,
  replaceOptions().expiry(found.expiryTime().get())
);
// end::expiry-replace[]
}

{
// tag::expiry-touch[]
collection.getAndTouch("my-document", Duration.ofDays(1));
// end::expiry-touch[]
}

  }

}
