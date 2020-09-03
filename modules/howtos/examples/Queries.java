// #tag::imports[]
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.*;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.MutationState;
import com.couchbase.client.java.query.*;
import org.reactivestreams.Subscription;
import reactor.core.publisher.*;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
// #end::imports[]


class Queries {

  static Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

  public static void main(String... args) {
    {
      // #tag::simple[]
      try {
        final QueryResult result = cluster
          .query("select * from `travel-sample` limit 10", queryOptions().metrics(true));

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println("Found row: " + row);
        }

        System.out.println("Reported execution time: "
          + result.metaData().metrics().get().executionTime());
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
      // #end::simple[]
    }

    {
      // #tag::named[]
      QueryResult result = cluster.query(
        "select count(*) from `travel-sample` where type = \"airport\" and country = $country",
        queryOptions().parameters(JsonObject.create().put("country", "France"))
      );
      // #end::named[]
    }


    {
      // #tag::positional[]
      QueryResult result = cluster.query(
        "select count(*) from `travel-sample` where type = \"airport\" and country = ?",
        queryOptions().parameters(JsonArray.from("France"))
      );
      // #end::positional[]
    }

    {
      // #tag::scanconsistency[]
      QueryResult result = cluster.query(
        "select ...",
        queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS)
      );
      // #end::scanconsistency[]
    }

    {
      // #tag::scanconsistency_with[]
      Bucket bucket = cluster.bucket("travel-sample");
      Collection collection = bucket.defaultCollection();
      MutationResult mr = collection.upsert("someDoc",JsonObject.create().put("name", "roi"));
      MutationState mutationState = MutationState.from(mr.mutationToken().get());
    
      QueryOptions qo = QueryOptions.queryOptions().consistentWith(mutationState); 
      QueryResult result = cluster.query(
        "select raw meta().id from `bucket1` limit 100;",qo
      );
      // #end::scanconsistency_with[]
    }

    {
      // #tag::clientcontextid[]
      QueryResult result = cluster.query(
        "select ...",
        queryOptions().clientContextId("user-44" + UUID.randomUUID().toString())
      );
      // #end::clientcontextid[]
    }

    {
      // #tag::readonly[]
      QueryResult result = cluster.query(
        "select ...",
        queryOptions().readonly(true)
      );
      // #end::readonly[]
    }

    {
      // #tag::printmetrics[]
      QueryResult result = cluster.query("select 1=1", queryOptions().metrics(true));
      System.err.println(
        "Execution time: " + result.metaData().metrics().get().executionTime()
      );
      // #end::printmetrics[]
    }

    {
      // #tag::rowsasobject[]
      QueryResult result = cluster.query(
        "select * from `travel-sample` limit 10"
      );
      for (JsonObject row : result.rowsAsObject()) {
        System.out.println("Found row: " + row);
      }
      // #end::rowsasobject[]
    }

    {
      // #tag::simplereactive[]
      Mono<ReactiveQueryResult> result = cluster
        .reactive()
        .query("select 1=1");

      result
        .flatMapMany(ReactiveQueryResult::rowsAsObject)
        .subscribe(row -> System.out.println("Found row: " + row));
      // #end::simplereactive[]
    }

    {
      // #tag::backpressure[]
      Mono<ReactiveQueryResult> result = cluster
        .reactive()
        .query("select * from hugeBucket");

      result
        .flatMapMany(ReactiveQueryResult::rowsAsObject)
        .subscribe(new BaseSubscriber<JsonObject>() {
          // Number of outstanding requests
          final AtomicInteger oustanding = new AtomicInteger(0);

          @Override
          protected void hookOnSubscribe(Subscription subscription) {
            request(10); // initially request to rows
            oustanding.set(10);
          }

          @Override
          protected void hookOnNext(JsonObject value) {
            process(value);
            if (oustanding.decrementAndGet() == 0) {
              request(10);
            }
          }
        });
      // #end::backpressure[]
    }

  }

  static void process(JsonObject value) {

  }

}
