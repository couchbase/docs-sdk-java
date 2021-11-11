/*
 * Copyright (c) 2021 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.List;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import reactor.core.publisher.Flux;

public class KvBulkHelloWorld {
  public static void main(String[] args) {
    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
    Bucket bucket = cluster.bucket("travel-sample");
    Collection collection = bucket.scope("tenant_agent_00").collection("users");

    ReactiveCollection reactiveCollection = collection.reactive();

    // tag::kv-users[]
    JsonObject user1 = JsonObject.create().put("id", "user_111").put("email", "tom_the_cat@gmail.com");
    JsonObject user2 = JsonObject.create().put("id", "user_222").put("email", "jerry_mouse@gmail.com");
    JsonObject user3 = JsonObject.create().put("id", "user_333").put("email", "mickey_mouse@gmail.com");

    List<JsonDocument> documents = new ArrayList<>();
    documents.add(new JsonDocument("user_111", user1));
    documents.add(new JsonDocument("user_222", user2));
    documents.add(new JsonDocument("user_333", user3));
    // end::kv-users[]

    {
      System.out.println("Example: [kv-bulk-insert]");
      // tag::kv-bulk-insert[]
      // Iterate over a list of documents to insert.
      List<MutationResult> results = Flux.fromIterable(documents)
          .flatMap(document -> reactiveCollection.insert(
              document.getId(), document.getContent()
            )
          )
          .collectList()
          .block(); // Wait until all operations have completed.

      // Print all the results.
      for (MutationResult result : results) {
        System.out.println("CAS: " + result.cas());
      }
      // end::kv-bulk-insert[]
    }

    {
      System.out.println("\nExample: [kv-bulk-upsert]");
      // tag::kv-bulk-upsert[]
      JsonObject newUser1 = JsonObject.create().put("id", "user_111").put("email", "tom@gmail.com");
      JsonObject newUser2 = JsonObject.create().put("id", "user_222").put("email", "jerry@gmail.com");
      JsonObject newUser3 = JsonObject.create().put("id", "user_333").put("email", "mickey@gmail.com");
      
      List<JsonDocument> newDocuments = new ArrayList<>();
      newDocuments.add(new JsonDocument("user_111", newUser1));
      newDocuments.add(new JsonDocument("user_222", newUser2));
      newDocuments.add(new JsonDocument("user_333", newUser3));

      // Iterate over a list of documents to upsert.
      List<MutationResult> results = Flux.fromIterable(newDocuments)
          .flatMap(newDocument -> reactiveCollection.upsert(
              newDocument.getId(), newDocument.getContent()
            )
          )
          .collectList()
          .block(); // Wait until all operations have completed.

      // Print all the results.
      for (MutationResult result : results) {
        System.out.println("CAS: " + result.cas());
      }
      // end::kv-bulk-upsert[]
    }

    {
      System.out.println("\nExample: [kv-bulk-get]");
      // tag::kv-bulk-get[]
      // Iterate over a list of documents to fetch.
      List<GetResult> results = Flux.fromIterable(documents)
          .flatMap(document -> reactiveCollection.get(document.getId()))
          .collectList()
          .block(); // Wait until all operations have completed.

      // Print all the results.
      for (GetResult result : results) {
        JsonObject document = result.contentAsObject();
        System.out.println("Document: "  + document);
        System.out.println("CAS: " + result.cas());
      }
      // end::kv-bulk-get[]
    }

    {
      System.out.println("\nExample: [kv-bulk-remove]");
      // tag::kv-bulk-remove[]
      // Iterate over a list of documents to remove.
      List<MutationResult> results = Flux.fromIterable(documents)
          .flatMap(document -> reactiveCollection.remove(document.getId()))
          .collectList()
          .block(); // Wait until all operations have completed.

      // Print all the results.
      for (MutationResult result : results) {
        System.out.println("CAS: " + result.cas());
      }
      // end::kv-bulk-remove[]
    }
  }
}


// tag::kv-bulk-class[]
class JsonDocument {
  private final String id;
  private final JsonObject content;

  public JsonDocument(String id, JsonObject content) {
    this.id = id;
    this.content = content;
  }

  public String getId() {
    return id;
  }

  public JsonObject getContent() {
    return content;
  }

  @Override
  public String toString() {
    return "JsonDocument{id='" + id + "', content=" + content + "}";
  }
}
// end::kv-bulk-class[]
