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
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.MutationState;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.query.ReactiveQueryResult;

import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
// end::imports[]

public class Queries {

  static Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

  public static void main(String... args) {
    {
      System.out.println("Example: [simple]");
      // tag::simple[]
      try {
        final QueryResult result = cluster.query("select * from `travel-sample` limit 10",
            queryOptions().metrics(true));

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println("Found row: " + row);
        }

        System.out.println("Reported execution time: " + result.metaData().metrics().get().executionTime());
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
      // end::simple[]
    }

    {
      System.out.println("\nExample: [named]");
      // tag::named[]
      QueryResult result = cluster.query(
          "select count(*) from `travel-sample` where type = \"airport\" and country = $country",
          queryOptions().parameters(JsonObject.create().put("country", "France")));
      // end::named[]
    }

    {
      System.out.println("\nExample: [positional]");
      // tag::positional[]
      QueryResult result = cluster.query(
          "select count(*) from `travel-sample` where type = \"airport\" and country = ?",
          queryOptions().parameters(JsonArray.from("France")));
      // end::positional[]
    }

    {
      System.out.println("\nExample: [scanconsistency]");
      // tag::scanconsistency[]
      QueryResult result = cluster.query(
          "select count(*) from `travel-sample` where type = \"airport\" and country = 'France'",
          queryOptions().scanConsistency(QueryScanConsistency.REQUEST_PLUS));
      // end::scanconsistency[]
    }

    {
      System.out.println("\nExample: [scanconsistency_with]");
      // tag::scanconsistency_with[]
      Bucket bucket = cluster.bucket("travel-sample");
      Collection collection = bucket.defaultCollection();
      MutationResult mr = collection.upsert("someDoc", JsonObject.create().put("name", "roi"));
      MutationState mutationState = MutationState.from(mr.mutationToken().get());

      QueryOptions qo = QueryOptions.queryOptions().consistentWith(mutationState);
      QueryResult result = cluster.query("select raw meta().id from `travel-sample` limit 100;", qo);
      // end::scanconsistency_with[]
    }

    {
      System.out.println("\nExample: [clientcontextid]");
      // tag::clientcontextid[]
      QueryResult result = cluster.query(
          "select count(*) from `travel-sample` where type = \"airport\" and country = 'France'",
          queryOptions().clientContextId("user-44" + UUID.randomUUID()));
      // end::clientcontextid[]
    }

    {
      System.out.println("\nExample: [readonly]");
      // tag::readonly[]
      QueryResult result = cluster.query(
          "select count(*) from `travel-sample` where type = \"airport\" and country = 'France'",
          queryOptions().readonly(true));
      // end::readonly[]
    }

    {
      System.out.println("\nExample: [printmetrics]");
      // tag::printmetrics[]
      QueryResult result = cluster.query("select 1=1", queryOptions().metrics(true));
      System.err.println("Execution time: " + result.metaData().metrics().get().executionTime());
      // end::printmetrics[]
    }

    {
      System.out.println("\nExample: [rowsasobject]");
      // tag::rowsasobject[]
      QueryResult result = cluster.query("select * from `travel-sample` limit 10");
      for (JsonObject row : result.rowsAsObject()) {
        System.out.println("Found row: " + row);
      }
      // end::rowsasobject[]
    }

    {
      System.out.println("\nExample: [simplereactive]");
      // tag::simplereactive[]
      Mono<ReactiveQueryResult> result = cluster.reactive().query("select 1=1");

      result.flatMapMany(ReactiveQueryResult::rowsAsObject).subscribe(row -> System.out.println("Found row: " + row));
      // end::simplereactive[]
    }

    {
      System.out.println("\nExample: [backpressure]");
      // tag::backpressure[]
      Mono<ReactiveQueryResult> result = cluster.reactive().query("select * from `travel-sample`");

      result.flatMapMany(ReactiveQueryResult::rowsAsObject).subscribe(new BaseSubscriber<JsonObject>() {
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
      // end::backpressure[]
    }

    {
      System.out.println("\nExample: [scope-level-query]");
      // tag::scope-level-query[]
      Bucket bucket = cluster.bucket("travel-sample");
      Scope scope = bucket.scope("inventory");

      QueryResult result = scope.query("select * from `airline` where country = $country LIMIT 10",
          queryOptions().parameters(JsonObject.create().put("country", "France")));

      for (JsonObject row : result.rowsAsObject()) {
        System.out.println("Found row: " + row);
      }
      // end::scope-level-query[]
    }

  }

  static void process(JsonObject value) {

  }

}
