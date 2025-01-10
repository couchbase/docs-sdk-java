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

 /*
  * You need the following datasets created:
  *
  *  CREATE DATASET `airport` ON `travel-sample` where `type` = "airport";
  *  CREATE DATASET `huge-dataset` ON `travel-sample`;
  *  ALTER COLLECTION `travel-sample`.`inventory`.`airport` ENABLE ANALYTICS;
  *  CONNECT LINK Local;
  */

// tag::imports[]
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.analytics.AnalyticsResult;
import com.couchbase.client.java.analytics.AnalyticsScanConsistency;
import com.couchbase.client.java.analytics.ReactiveAnalyticsResult;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.couchbase.client.java.analytics.AnalyticsOptions.analyticsOptions;
// end::imports[]

public class Analytics {

  static Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

  public static void show(String description, AnalyticsResult result) {
    System.out.println(description);

    for (JsonObject row : result.rowsAsObject()) {
      System.out.println("Found row: " + row);
    }

    System.out.println("Reported execution time: "
      + result.metaData().metrics().executionTime());

      System.out.println();
  }


  public static void main(String... args) {
    {
      // tag::simple[]
      try {
        final AnalyticsResult result = cluster
          .analyticsQuery("select \"hello\" as greeting");

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println("Found row: " + row);
        }

        System.out.println("Reported execution time: "
          + result.metaData().metrics().executionTime());

        System.out.println();

      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
      // end::simple[]
    }

    {
      // tag::named[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = $country",
        analyticsOptions().parameters(JsonObject.create().put("country", "France")));
      // end::named[]
      show("named", result);
    }

    {
      // tag::positional[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = ?",
        analyticsOptions().parameters(JsonArray.from("France"))
      );
      // end::positional[]
      show("positional", result);
    }

    {
      // tag::scanconsistency[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = 'France'",
        analyticsOptions().scanConsistency(AnalyticsScanConsistency.REQUEST_PLUS)
      );
      // end::scanconsistency[]
      show("scanconsistency", result);
    }

    {
      // tag::clientcontextid[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = 'France'",
        analyticsOptions().clientContextId("user-44" + UUID.randomUUID())
      );
      // end::clientcontextid[]
      show("clientcontextid", result);
    }

    {
      // tag::priority[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = 'France'",
        analyticsOptions().priority(true)
      );
      // end::priority[]
      show("priority", result);
    }

    {
      // tag::readonly[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airport where country = 'France'",
        analyticsOptions().readonly(true)
      );
      // end::readonly[]
      show("readonly", result);
    }

    {
      // tag::printmetrics[]
      AnalyticsResult result = cluster.analyticsQuery("select 1=1");
      System.err.println(
        "Execution time: " + result.metaData().metrics().executionTime()
      );
      // end::printmetrics[]
    }

    {
      // tag::rowsasobject[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select * from airport limit 3"
      );
      for (JsonObject row : result.rowsAsObject()) {
        System.out.println("Found row: " + row);
      }
      // end::rowsasobject[]
      System.out.println();
    }

    {
      // tag::handle-collection[]
      AnalyticsResult result = cluster.analyticsQuery(
        "SELECT airportname, country FROM `travel-sample`.inventory.airport WHERE country='France' LIMIT 3");
      // end::handle-collection[]
      show("handle-collection", result);
    }

    {
      // tag::handle-scope[]
      Bucket bucket = cluster.bucket("travel-sample");
      Scope scope = bucket.scope("inventory");
      AnalyticsResult result = scope.analyticsQuery(
        "SELECT airportname, country FROM `airport` WHERE country='France' LIMIT 4");
      // end::handle-scope[]
      show("handle-scope", result);
    }

    System.out.println("Disconnecting...");
    cluster.disconnect();
  }

  public static void asyncExamples(String... args) {
    {
      {
        System.out.println("simplereactive");
        // tag::simplereactive[]
        Mono<ReactiveAnalyticsResult> result = cluster
          .reactive()
          .analyticsQuery("select 1=1");

        result
          .flatMapMany(ReactiveAnalyticsResult::rowsAsObject)
          .subscribe(row -> System.out.println("Found row: " + row));
        // end::simplereactive[]
      }

      System.out.println("backpressure");
      // tag::backpressure[]
      Mono<ReactiveAnalyticsResult> result = cluster
        .reactive()
        .analyticsQuery("select * from `huge-dataset`");

      result
        .flatMapMany(ReactiveAnalyticsResult::rowsAsObject)
        .subscribe(new BaseSubscriber<JsonObject>() {
          // Number of outstanding requests
          final AtomicInteger outstanding = new AtomicInteger(0);

          @Override
          protected void hookOnSubscribe(Subscription subscription) {
            request(10); // initially request to rows
            outstanding.set(10);
          }

          @Override
          protected void hookOnNext(JsonObject value) {
            process(value);
            if (outstanding.decrementAndGet() == 0) {
              request(10);
              outstanding.set(10);
            }
          }
      });
      // end::backpressure[]
    }

  }

  static void process(JsonObject value) {

  }

}
