import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DocumentsExample {

    public static void main(String[] args) {

        Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

        Bucket bucket = cluster.bucket("travel-sample");
        Scope scope = bucket.scope("inventory");
        Collection collection = scope.collection("airline");

        {
            System.out.println("Example - [mutate-in]");
            // tag::mutate-in[]
            List<MutateInSpec> spec = Collections.singletonList(
                    MutateInSpec.upsert("msrp", 18.00)
            );
            collection.mutateIn("airline_10", spec);
            // end::mutate-in[]
        }

        {
            System.out.println("Example - [lookup-in]");
            // tag::lookup-in[]
            Collection usersCollection = bucket.scope("tenant_agent_00").collection("users");
            List<LookupInSpec> spec = Arrays.asList(
                    LookupInSpec.get("credit_cards[0].type"),
                    LookupInSpec.get("credit_cards[0].expiration")
            );
            usersCollection.lookupIn("1", spec);
            // end::lookup-in[]
        }

        {
            System.out.println("Example - [counters]");
            // tag::counters[]
            String counterDocId = "counter-doc";
            // Increment by 1, creating doc if needed.
            // By using `.incrementOptions().initial(1)` we set the starting count(non-negative) to 1 if the document needs to be created.
            // If it already exists, the count will increase by 1.
            collection.binary().increment(counterDocId, IncrementOptions.incrementOptions().initial(1));
            // Decrement by 1
            collection.binary().decrement(counterDocId);
            // Decrement by 5
            collection.binary().decrement(counterDocId, DecrementOptions.decrementOptions().delta(5));
            // end::counters[]
        }

        {
            System.out.println("Example - [counter-increment]");
            // tag::counter-increment[]
            GetResult getResult = collection.get("counter-doc");
            int value = getResult.contentAs(Integer.class);
            int incrementAmnt = 5;

            if (shouldIncrementAmnt(value)) {
                collection.replace(
                        "counter-doc",
                        value + incrementAmnt,
                        ReplaceOptions.replaceOptions().cas(getResult.cas())
                );
            }
            // end::counter-increment[]
            System.out.println("RESULT: " + value+incrementAmnt);
        }
    }

    private static boolean shouldIncrementAmnt(int value) {
        System.out.println("Current value: " + value);
        return value == 0;
    }
}
