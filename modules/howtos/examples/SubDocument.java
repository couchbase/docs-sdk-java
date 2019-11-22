// #tag::imports[]
import com.couchbase.client.core.error.subdoc.PathExistsException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Collections;

import static com.couchbase.client.java.kv.LookupInSpec.*;
import static com.couchbase.client.java.kv.MutateInOptions.mutateInOptions;
import static com.couchbase.client.java.kv.MutateInSpec.*;
// #end::imports[]

class SubDocument {
    static Collection collection;

    public static void main(String... args) {

        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

        Bucket bucket = cluster.bucket("bucket-name");
        Scope scope = bucket.scope("scope-name");
        collection = scope.collection("collection-name");
    }

    static void getFunc() {
// #tag::get[]
LookupInResult result = collection.lookupIn(
  "customer123",
  Collections.singletonList(get("addresses.delivery.country"))
);

String str = result.contentAs(0, String.class);
System.out.println("Country = " + str);
// #end::get[]
    }

    static void existsFunc() {
// #tag::exists[]
        LookupInResult result = collection.lookupIn("customer123",
                Arrays.asList(exists("addresses.delivery.does_not_exist")));

        boolean exists = result.contentAs(0, Boolean.class);
// #end::exists[]
    }

    static void combine() {
// #tag::combine[]
        LookupInResult result = collection.lookupIn("customer123",
                Arrays.asList(
                        get("addresses.delivery.country"),
                        exists("addresses.delivery.does_not_exist")
                ));

        String country = result.contentAs(0, String.class);
        boolean exists = result.contentAs(1, Boolean.class);
// #end::combine[]
    }

    static void future() throws ExecutionException, InterruptedException {
// #tag::get-future[]
        CompletableFuture<LookupInResult> future =
                collection.async().lookupIn("customer123", Arrays.asList(
                        get("addresses.delivery.country")
                ));

        // Just for example, block on the result - this is not best practice
        LookupInResult result = future.get();
// #end::get-future[]
    }

    static void reactive() {
// #tag::get-reactive[]
        Mono<LookupInResult> mono =
                collection.reactive().lookupIn("customer123",
                        Arrays.asList(
                                get("addresses.delivery.country")
                        ));

        // Just for example, block on the result - this is not best practice
        LookupInResult result = mono.block();
// #end::get-reactive[]
    }


    static void upsertFunc() {
// #tag::upsert[]
        collection.mutateIn("customer123", Arrays.asList(
                upsert("email", "dougr96@hotmail.com")
        ));

// #end::upsert[]
    }

    static void insertFunc() {
// #tag::insert[]
        try {
            collection.mutateIn("customer123", Arrays.asList(
                    insert("email", "dougr96@hotmail.com")
            ));
        } catch (PathExistsException err) {
            System.out.println("Error, path already exists");
        }
// #end::insert[]
    }

    static void multiFunc() {
// #tag::multi[]
        collection.mutateIn("customer123", Arrays.asList(
                remove("addresses.billing"),
                replace("email", "dougr96@hotmail.com")
        ));
// #end::multi[]
    }


    static void arrayAppendFunc() {
// #tag::array-append[]
        collection.mutateIn("customer123", Arrays.asList(
                arrayAppend("purchases.complete", Collections.singletonList(777))
        ));

        // purchases.complete is now [339, 976, 442, 666, 777]
// #end::array-append[]
    }

    static void arrayPrependFunc() {
// #tag::array-prepend[]
        collection.mutateIn("customer123", Arrays.asList(
                arrayPrepend("purchases.abandoned", Collections.singletonList(18))
        ));

        // purchases.abandoned is now [18, 157, 49, 999]
// #end::array-prepend[]
    }

    static void createAndPopulateArrays() {
// #tag::array-create[]
        collection.upsert("my_array", JsonArray.create());

        collection.mutateIn("my_array", Arrays.asList(
                arrayAppend("", Collections.singletonList("some element"))
        ));
        // the document my_array is now ["some element"]
// #end::array-create[]
    }

    static void arrayCreate() {
// #tag::array-upsert[]
        collection.mutateIn("some_doc", Arrays.asList(
                arrayAppend("some.array", Collections.singletonList("hello world")).createPath()
        ));
// #end::array-upsert[]
    }

    static void arrayUnique() {
// #tag::array-unique[]
        collection.mutateIn("customer123", Arrays.asList(
                arrayAddUnique("purchases.complete", 95)
        ));

        try {
            collection.mutateIn("customer123", Arrays.asList(
                    arrayAddUnique("purchases.complete", 95)
            ));
        } catch (PathExistsException err) {
            System.out.println("Error, path already exists");
        }

// #end::array-unique[]
    }

    static void arrayInsertFunc() {
// #tag::array-insert[]
        collection.mutateIn("some_doc", Arrays.asList(
                arrayInsert("foo.bar[1]", Collections.singletonList("cruel"))
        ));
// #end::array-insert[]
    }

    static void counterInc() {
// #tag::counter-inc[]
        MutateInResult result = collection.mutateIn("customer123", Arrays.asList(
                increment("logins", 1)
        ));

        // Counter operations return the updated count
        Long count = result.contentAs(0, Long.class);
// #end::counter-inc[]
    }

    static void counterDec() {
// #tag::counter-dec[]
        collection.upsert("player432", JsonObject.create().put("gold", 1000));

        collection.mutateIn("player432", Arrays.asList(
                decrement("gold", 150)
        ));
// #end::counter-dec[]
    }

    static void createPath() {
// #tag::create-path[]
        collection.mutateIn("customer123", Arrays.asList(
                upsert("level_0.level_1.foo.bar.phone",
                        JsonObject.create()
                                .put("num", "311-555-0101")
                                .put("ext", 16))
                        .createPath()
        ));
// #end::create-path[]
    }

    static void concurrent() {
// #tag::concurrent[]
        // Thread 1
        collection.mutateIn("customer123", Arrays.asList(
                arrayAppend("purchases.complete", Collections.singletonList(99))
        ));

        // Thread 2
        collection.mutateIn("customer123", Arrays.asList(
                arrayAppend("purchases.abandoned", Collections.singletonList(101))
        ));
// #end::concurrent[]


    }

    static void cas() {
// #tag::cas[]
GetResult doc = collection.get("player432");
collection.mutateIn(
  "player432",
  Arrays.asList(decrement("gold", 150)),
  mutateInOptions().cas(doc.cas())
);
        // #end::cas[]
    }


    static void oldDurability() {
// #tag::old-durability[]
collection.mutateIn(
  "key",
  Collections.singletonList(MutateInSpec.insert("foo", "bar")),
  mutateInOptions().durability(PersistTo.ACTIVE, ReplicateTo.ONE)
);
// #end::old-durability[]
    }

    static void newDurability() {
// #tag::new-durability[]
collection.mutateIn(
  "key",
  Collections.singletonList(MutateInSpec.insert("foo", "bar")),
  mutateInOptions().durability(DurabilityLevel.MAJORITY)
);
// #end::new-durability[]
    }

}
