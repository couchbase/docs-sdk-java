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
import static com.couchbase.client.java.kv.LookupInSpec.exists;
import static com.couchbase.client.java.kv.LookupInSpec.get;
import static com.couchbase.client.java.kv.MutateInOptions.mutateInOptions;
import static com.couchbase.client.java.kv.MutateInSpec.arrayAddUnique;
import static com.couchbase.client.java.kv.MutateInSpec.arrayAppend;
import static com.couchbase.client.java.kv.MutateInSpec.arrayInsert;
import static com.couchbase.client.java.kv.MutateInSpec.arrayPrepend;
import static com.couchbase.client.java.kv.MutateInSpec.decrement;
import static com.couchbase.client.java.kv.MutateInSpec.increment;
import static com.couchbase.client.java.kv.MutateInSpec.insert;
import static com.couchbase.client.java.kv.MutateInSpec.remove;
import static com.couchbase.client.java.kv.MutateInSpec.upsert;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.couchbase.client.core.error.CasMismatchException;
import com.couchbase.client.core.error.DurabilityImpossibleException;
import com.couchbase.client.core.error.subdoc.PathExistsException;
import com.couchbase.client.core.error.subdoc.PathNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.LookupInResult;
import com.couchbase.client.java.kv.MutateInResult;
import com.couchbase.client.java.kv.MutateInSpec;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;

import reactor.core.publisher.Mono;
// end::imports[]

public class SubDocument {
  static Collection collection;

  public static void main(String... args) {

    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

    Bucket bucket = cluster.bucket("travel-sample");
    Scope scope = bucket.scope("inventory");
    collection = scope.collection("hotel");
    bucket.waitUntilReady(Duration.ofSeconds(10));
    getFunc();
    existsFunc();
    combine();
    future();
    reactive();
    upsertFunc();
    insertFunc();
    multiFunc();
    arrayAppendObjFunc();
    arrayAppendFunc();
    arrayPrependObjFunc();
    arrayPrependFunc();
    full_doc_replaceFunc();
    createAndPopulateArrays();
    arrayCreate();
    arrayUnique();
    arrayInsertFunc();
    counterInc();
    counterDec();
    createPath();
    concurrent();
    cas();
    cas_fail();
    oldDurability();
    newDurability();
  }

  static void getFunc() {
    // tag::get[]
    LookupInResult result = collection.lookupIn("hotel_1368", Collections.singletonList(get("geo.lat")));

    String str = result.contentAs(0, String.class);
    System.out.println("getFunc: Latitude = " + str);
    // end::get[]
  }

  static void existsFunc() {

    // tag::exists[]
    try {
      LookupInResult result = collection.lookupIn("hotel_1368",
          Collections.singletonList(exists("address.does_not_exist")));
    } catch (PathNotFoundException e) {
      System.out.println("existsFunc: " + e);
    }
    // end::exists[]
  }

  static void combine() {
    // tag::combine[]
    try {
      LookupInResult result = collection.lookupIn("hotel_1368",
          Arrays.asList(get("geo.lat"), exists("address.does_not_exist")));
    } catch (PathNotFoundException e) {
      System.out.println("combine: " + e);
    }
    // end::combine[]
  }

  static void future() {
    // tag::get-future[]
    CompletableFuture<LookupInResult> future = collection.async().lookupIn("hotel_1368",
        Collections.singletonList(get("geo.lat")));

    try {
      LookupInResult result = future.get();
      System.out.println("future: Latitude: " + result.contentAs(0, Number.class));
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    // end::get-future[]
  }

  static void reactive() {
    // tag::get-reactive[]
    Mono<LookupInResult> mono = collection.reactive().lookupIn("hotel_1368",
        Collections.singletonList(get("geo.lat")));

    // Just for example, block on the result - this is not best practice
    LookupInResult result = mono.block();
    // end::get-reactive[]
    System.out.println("reactive: Latitude: " + result.contentAs(0, Number.class));
  }

  static void upsertFunc() {
    // tag::upsert[]
    collection.mutateIn("hotel_1368", Arrays.asList(upsert("email", "hotel96@hotmail.com")));
    // end::upsert[]
    System.out.println("upsertFunc: hotel_1368 email");
  }

  static void insertFunc() {
    // tag::insert[]
    try {
      collection.mutateIn("hotel_1368", Collections.singletonList(insert("alt_email", "alt_hotel96@hotmail.com")));
    } catch (PathExistsException err) {
      System.out.println("insertFunc: exception caught, path already exists");
    }
    // end::insert[]
  }

  static void multiFunc() {
    try {
      collection.mutateIn("hotel_1368", Arrays.asList(upsert("tz", "EST")));
    } catch (PathExistsException e) {
    }
    try {
      collection.mutateIn("hotel_1368", Arrays.asList(remove("alt_email")));
    } catch (PathNotFoundException e) {
    }
    // tag::multi[]
    collection.mutateIn("hotel_1368", Arrays.asList(remove("tz"), insert("alt_email", "hotel84@hotmail.com")));
    // end::multi[]
    System.out.println("upsertFunc: hotel_1368 alt_email");
  }

  static void arrayAppendObjFunc() {

    JsonObject docContent = JsonObject.create();
    docContent.put("content", "very nice hotel");
    docContent.put("author", "Nedra Cronin");
    docContent.put("date", "2012-01-14 02:15:51 +0300");

    // tag::array-appendobj[]
    collection.mutateIn("hotel_1368",
        Collections.singletonList(arrayAppend("reviews", Collections.singletonList(docContent))));
    // end::array-appendobj[]
    System.out.println("arrayAppendObjFunc: hotel_1368 reviews");
  }

  static void arrayAppendFunc() {
    // tag::array-append[]
    MutationResult result = collection.mutateIn("hotel_1368",
        Collections.singletonList(arrayAppend("public_likes", Collections.singletonList("Mike Rutherford"))));
    /*
      public_likes is now:
      ["Georgette Rutherford V", "Ms. Devante Bruen", "Anderson Schmidt", "Mr. Kareem Harvey", "Tessie Shields",
      "Floyd Bradtke III", "Maurice McDermott", "Michel Franecki", "Laila Ernser", "Mike Rutherford"]
    */
    // end::array-append[]
  }

  static void arrayPrependObjFunc() {
    JsonObject docContent = JsonObject.create();
    docContent.put("content", "not a very nice hotel");
    docContent.put("author", "Terry Smith");
    docContent.put("date", "2013-01-14 03:16:51 +0300");

    // tag::array-prependobj[]
    collection.mutateIn("hotel_1501",
        Collections.singletonList(arrayPrepend("reviews", Collections.singletonList(docContent))));
    // end::array-prependobj[]
    System.out.println("arrayAppendObjFunc: hotel_1501 reviews");
  }

  static void arrayPrependFunc() {
    // tag::array-prepend[]
    MutationResult result = collection.mutateIn("hotel_1368",
        Collections.singletonList(arrayPrepend("public_likes", Collections.singletonList("John Smith"))));

    /*
      public_likes is now:
      ["John Smith", "Georgette Rutherford V", "Ms. Devante Bruen", "Anderson Schmidt", "Mr. Kareem Harvey", "Tessie Shields",
      "Floyd Bradtke III", "Maurice McDermott", "Michel Franecki", "Laila Ernser", "Mike Rutherford"]
    */
    // end::array-prepend[]
  }

  static void full_doc_replaceFunc() {
    // tag::full_doc_replace[]
    JsonObject docContent = JsonObject.create().put("body", "value");
    collection.mutateIn("hotel_14006",
        Arrays.asList(MutateInSpec.upsert("foo", "bar").xattr().createPath(), MutateInSpec.replace("", docContent)));
    // end::full_doc_replace[]
    System.out.println("full_doc_replaceFunc: hotel_14006 foo");

  }

  static void createAndPopulateArrays() {
    // tag::array-create[]
    collection.upsert("my_array", JsonArray.create());

    collection.mutateIn("my_array",
        Collections.singletonList(arrayAppend("", Collections.singletonList("some element"))));
    // the document my_array is now ["some element"]
    // end::array-create[]
    System.out.println("createAndPopulateArrays: my_array some element");

  }

  static void arrayCreate() {
    // tag::array-upsert[]
    MutateInResult result = collection.mutateIn("hotel_14225",
        Collections.singletonList(arrayAppend("some.array", Collections.singletonList("hello world")).createPath()));
    // end::array-upsert[]
    System.out.println("arrayCreate: hotel_14225 some.array " + result);

  }

  static void arrayUnique() {
    collection.mutateIn("hotel_14226", Arrays.asList(MutateInSpec.upsert("unique", Collections.singletonList(88))));
    // tag::array-unique[]
    collection.mutateIn("hotel_14226", Collections.singletonList(arrayAddUnique("unique", 95)));

    try {
      collection.mutateIn("hotel_14226", Collections.singletonList(arrayAddUnique("unique", 95)));
      throw new RuntimeException("should have thrown PathExistsException");
    } catch (PathExistsException err) {
      System.out.println("arrayUnique: caught exception, path already exists");
    }
    // end::array-unique[]
  }

  static void arrayInsertFunc() {
    collection.mutateIn("hotel_1501", Arrays.asList(MutateInSpec.upsert("foo", Collections.singletonList(88))));
    // tag::array-insert[]
    MutateInResult result = collection.mutateIn("hotel_1501",
        Collections.singletonList(arrayInsert("foo[1]", Collections.singletonList("cruel"))));
    // end::array-insert[]
    System.out.println("arrayInsertFunc: hotel_1501 foo " + result);

  }

  static void counterInc() {
    collection.mutateIn("hotel_1368", Arrays.asList(MutateInSpec.upsert("logins", 1)));
    // tag::counter-inc[]
    MutateInResult result = collection.mutateIn("hotel_1368", Collections.singletonList(increment("logins", 1)));

    // Counter operations return the updated count
    Long count = result.contentAs(0, Long.class);
    // end::counter-inc[]
    System.out.println("counterInc: hotel_1368 logins " + count);

  }

  static void counterDec() {
    collection.mutateIn("hotel_1368", Arrays.asList(MutateInSpec.upsert("logouts", 1000)));
    // tag::counter-dec[]

    MutateInResult result = collection.mutateIn("hotel_1368", Collections.singletonList(decrement("logouts", 150)));
    // Counter operations return the updated count
    Long count = result.contentAs(0, Long.class);
    // end::counter-dec[]
    System.out.println("counterDec: hotel_1368 logouts " + count);

  }

  static void createPath() {
    // tag::create-path[]
    MutateInResult result = collection.mutateIn("hotel_1368",
        Collections.singletonList(
            upsert("level_0.level_1.foo.bar.phone", JsonObject.create().put("num", "311-555-0101").put("ext", 16))
                .createPath()));
    // end::create-path[]
    System.out.println("createPath: hotel_1368 " + result);
  }

  static void concurrent() {
    // tag::concurrent[]
    Thread thread1 = new Thread() {
      public void run() {
        collection.mutateIn("hotel_1501",
            Collections.singletonList(arrayAppend("foo", Collections.singletonList(99))));
      }
    };

    Thread thread2 = new Thread() {
      public void run() {
        collection.mutateIn("hotel_1501",
            Collections.singletonList(arrayAppend("foo", Collections.singletonList(101))));
      }
    };
    thread1.start();
    thread2.start();
    // end::concurrent[]
    try {
      thread1.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    try {
      thread2.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    GetResult doc = collection.get("hotel_1501");
    System.out.println("concurrent: hotel_1501 " + doc);

  }

  static void cas() {
    // tag::cas[]
    GetResult doc = collection.get("hotel_1368");
    MutationResult result = collection.mutateIn("hotel_1368", Collections.singletonList(decrement("logouts", 150)),
        mutateInOptions().cas(doc.cas()));
    // end::cas[]
    System.out.println("cas: hotel_1368 " + doc);
  }

  static void cas_fail() {
    // tag::cas_fail[]
    GetResult doc = collection.get("hotel_1368");
    try {
      MutationResult result = collection.mutateIn("hotel_1368", Collections.singletonList(decrement("logouts", 150)),
          mutateInOptions().cas(doc.cas() + 1));
    } catch (CasMismatchException e) {
      System.out.println("cas_fail: " + e);
    }
    // end::cas_fail[]
  }

  static void oldDurability() {
    try {
      // tag::old-durability[]
      MutationResult result = collection.mutateIn("hotel_1368",
          Collections.singletonList(MutateInSpec.upsert("foo", "bar")),
          mutateInOptions().durability(PersistTo.ACTIVE, ReplicateTo.ONE));
      // end::old-durability[]
      System.out.println("oldDurability: " + result);
    } catch (RuntimeException e) {
      System.out.println("oldDurability: replication not possible: " + e);
    }
  }

  static void newDurability() {
    try {
      // tag::new-durability[]
      MutationResult result = collection.mutateIn("hotel_1368",
          Collections.singletonList(MutateInSpec.upsert("foo", "bar")),
          mutateInOptions().durability(DurabilityLevel.MAJORITY));
      // end::new-durability[]
      System.out.println("newDurability: " + result);
    } catch (DurabilityImpossibleException e) {
      System.out.println("newDurability: replication not possible: " + e);
    }

  }

}
