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

// #tag::imports[]
import com.couchbase.client.core.error.CouchbaseException;
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
// #end::imports[]

public class Analytics {

  static Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

  public static void main(String... args) {
    {
      // #tag::simple[]
      try {
        final AnalyticsResult result = cluster
          .analyticsQuery("select \"hello\" as greeting");

        for (JsonObject row : result.rowsAsObject()) {
          System.out.println("Found row: " + row);
        }

        System.out.println("Reported execution time: "
          + result.metaData().metrics().executionTime());
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
      // #end::simple[]
    }

    {
      // #tag::named[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airports where country = $country",
        analyticsOptions().parameters(JsonObject.create().put("country", "France"))
      );
      // #end::named[]
    }


    {
      // #tag::positional[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select count(*) from airports where country = ?",
        analyticsOptions().parameters(JsonArray.from("France"))
      );
      // #end::positional[]
    }

    {
      // #tag::scanconsistency[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select ...",
        analyticsOptions().scanConsistency(AnalyticsScanConsistency.REQUEST_PLUS)
      );
      // #end::scanconsistency[]
    }

    {
      // #tag::clientcontextid[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select ...",
        analyticsOptions().clientContextId("user-44" + UUID.randomUUID())
      );
      // #end::clientcontextid[]
    }

    {
      // #tag::priority[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select ...",
        analyticsOptions().priority(true)
      );
      // #end::priority[]
    }

    {
      // #tag::readonly[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select ...",
        analyticsOptions().readonly(true)
      );
      // #end::readonly[]
    }

    {
      // #tag::printmetrics[]
      AnalyticsResult result = cluster.analyticsQuery("select 1=1");
      System.err.println(
        "Execution time: " + result.metaData().metrics().executionTime()
      );
      // #end::printmetrics[]
    }

    {
      // #tag::rowsasobject[]
      AnalyticsResult result = cluster.analyticsQuery(
        "select * from `travel-sample` limit 10"
      );
      for (JsonObject row : result.rowsAsObject()) {
        System.out.println("Found row: " + row);
      }
      // #end::rowsasobject[]
    }

    {
      // #tag::simplereactive[]
      Mono<ReactiveAnalyticsResult> result = cluster
        .reactive()
        .analyticsQuery("select 1=1");

      result
        .flatMapMany(ReactiveAnalyticsResult::rowsAsObject)
        .subscribe(row -> System.out.println("Found row: " + row));
      // #end::simplereactive[]
    }

    {
      // #tag::backpressure[]
      Mono<ReactiveAnalyticsResult> result = cluster
        .reactive()
        .analyticsQuery("select * from hugeDataset");

      result
        .flatMapMany(ReactiveAnalyticsResult::rowsAsObject)
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
              oustanding.set(10);
            }
          }
      });
      // #end::backpressure[]
    }

  }

  static void process(JsonObject value) {

  }

}
