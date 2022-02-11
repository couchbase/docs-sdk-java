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
import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.InsertOptions.insertOptions;
import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Optional;

import com.couchbase.client.core.error.CasMismatchException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.error.DurabilityImpossibleException;
import com.couchbase.client.core.error.ReplicaNotConfiguredException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.AsyncCollection;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;
// end::imports[]

import com.couchbase.client.core.error.FeatureNotAvailableException;

public class KvOperations {

  public static void main(String... args) {

    Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

    Bucket bucket = cluster.bucket("travel-sample");
    Scope scope = bucket.scope("_default");
    Collection collection = scope.collection("_default");

    JsonObject json = JsonObject.create()
        .put("title", "My Blog Post")
        .put("author", "mike");

    // tag::apis[]
    AsyncCollection asynccollection = collection.async();
    ReactiveCollection reactivecollection = collection.reactive();
    // end::apis[]

    {
      System.out.println("\nExample: [upsert]");
      // tag::upsert[]
      JsonObject content = JsonObject.create()
          .put("author", "mike")
          .put("title", "My Blog Post 1");

      MutationResult result = collection.upsert("document-key", content);
      // end::upsert[]
    }

    {
      System.out.println("\nExample: [insert]");
      // tag::insert[]
      try {
        JsonObject content = JsonObject.create()
            .put("title", "My Blog Post 2");
        MutationResult insertResult = collection.insert("document-key2", content);
      } catch (DocumentExistsException ex) {
        System.err.println("The document already exists!");
      } catch (CouchbaseException ex) {
        System.err.println("Something else happened: " + ex);
      }
      // end::insert[]
    }

    {
      System.out.println("\nExample: [get-simple]");
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
      System.out.println("\nExample: [get]");
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
      System.out.println("\nExample: [replace]");
      // tag::replace[]
      collection.upsert("my-document", JsonObject.create().put("initial", true));

      GetResult result = collection.get("my-document");
      JsonObject content = result.contentAsObject();
      content.put("modified", true).put("initial", false);
      collection.replace("my-document", content, replaceOptions().cas(result.cas()));
      // end::replace[]
    }

    {
      System.out.println("\nExample: [replace-retry]");
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
      System.out.println("\nExample: [remove]");
      // tag::remove[]
      try {
        collection.remove("my-document");
      } catch (DocumentNotFoundException ex) {
        System.out.println("Document did not exist when trying to remove");
      }
      // end::remove[]
    }

    {
      System.out.println("\nExample: [durability]");
      try {
        // tag::durability[]
        collection.upsert("my-document", JsonObject.create().put("doc", true),
            upsertOptions().durability(DurabilityLevel.MAJORITY));
        // end::durability[]
      } catch (DurabilityImpossibleException di) {
        System.out.println(di);
      }
    }

    {
      System.out.println("\nExample: [durability-observed]");
      try {
        // tag::durability-observed[]
        collection.upsert("my-document", JsonObject.create().put("doc", true),
            upsertOptions().durability(PersistTo.NONE, ReplicateTo.TWO));
        // end::durability-observed[]
      } catch (ReplicaNotConfiguredException rnc) {
        System.out.println(rnc);
      }
    }

    {
      System.out.println("\nExample: [expiry-insert]");
      // tag::expiry-insert[]
      MutationResult insertResult = collection.insert("my-document2", json,
          insertOptions().expiry(Duration.ofHours(2)));
      // end::expiry-insert[]
    }

    {
      System.out.println("\nExample: [expiry-insert-instant]");
      // tag::expiry-insert-instant[]
      MutationResult insertResult = collection.insert("my-document3", json,
          insertOptions().expiry(Instant.now().plus(Period.ofDays(62))));
      // end::expiry-insert-instant[]
    }

    {
      System.out.println("\nExample: [expiry-get]");
      // tag::expiry-get[]
      GetResult result = collection.get("my-document3", getOptions().withExpiry(true));
      Optional<Instant> expiry = result.expiryTime();
      System.out.println("Expiry of found doc: " + expiry);
      // end::expiry-get[]

      System.out.println("cas value: " + result.cas());
    }

    {
      System.out.println("\nExample: [expiry-replace]");
      // tag::expiry-replace[]
      GetResult found = collection.get("my-document3", getOptions().withExpiry(true));

      MutationResult result = collection.replace("my-document3", json,
          replaceOptions().expiry(found.expiryTime().get()));
      // end::expiry-replace[]

    }

    try {
      System.out.println("\nExample: [preserve-expiry]");

      // tag::preserve-expiry[]
      collection.replace("my-document3", json,
          replaceOptions().preserveExpiry(true));
      // end::preserve-expiry[]
    } catch (FeatureNotAvailableException e){
      System.out.println("Couldn't run preserveExpiry example: " + e);
    }

    {
      System.out.println("\nExample: [expiry-touch]");
      // tag::expiry-touch[]
      GetResult result = collection.getAndTouch("my-document3", Duration.ofDays(1));
      // end::expiry-touch[]

      System.out.println("cas value: " + result.cas());
    }

    {
      System.out.println("\nExample: [named-collection-upsert]");
      // tag::named-collection-upsert[]
      Scope agentScope = bucket.scope("tenant_agent_00");
      Collection usersCollection = agentScope.collection("users");

      JsonObject content = JsonObject.create().put("name", "John Doe").put("preferred_email",
          "johndoe111@test123.test");
      MutationResult result = usersCollection.upsert("user-key", content);
      // end::named-collection-upsert[]

      System.out.println("cas value: " + result.cas());
    }

    // Cleans up example data from the _default collection
    // to avoid errors when running the sample code (mainly for `insert` examples).
    cleanupData(collection);
  }

  private static void cleanupData(Collection collection) throws CouchbaseException {
    System.out.println("\nCleaning up example data....");

    collection.remove("document-key2");
    collection.remove("my-document2");
    collection.remove("my-document3");
  }
}
