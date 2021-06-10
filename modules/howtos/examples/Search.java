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

import static com.couchbase.client.java.search.SearchOptions.searchOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.MutationState;
import com.couchbase.client.java.search.HighlightStyle;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.facet.SearchFacet;
import com.couchbase.client.java.search.result.ReactiveSearchResult;
import com.couchbase.client.java.search.result.SearchResult;
import com.couchbase.client.java.search.result.SearchRow;
import com.couchbase.client.java.search.sort.SearchSort;

import org.reactivestreams.Subscription;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

// This example assumes an index called `travel-sample-index` exists.
// Please refer to file `modules/test/scripts/init-couchbase/init-buckets.sh` (line 66)
// for the relevant curl command to create this.

public class Search {

  static Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
  static Bucket bucket = cluster.bucket("travel-sample");
  static Scope scope = bucket.scope("inventory");
  static Collection collection = scope.collection("hotel");

  public static void main(String... args) {

    {
      // tag::simple[]
      try {
        final SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"));

        for (SearchRow row : result.rows()) {
          System.out.println("Found row: " + row);
        }

        System.out.println("Reported total rows: " + result.metaData().metrics().totalRows());
      } catch (CouchbaseException ex) {
        ex.printStackTrace();
      }
      // end::simple[]
    }

    {
      // tag::squery[]
      final SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.prefix("swim"),
          searchOptions().fields("description"));

      for (SearchRow row : result.rows()) {
        System.out.println("Score: " + row.score());
        System.out.println("Document Id: " + row.id());

        // Also print fields that are included in the query
        System.out.println(row.fieldsAs(JsonObject.class));
      }
      // end::squery[]
    }

    {
      // tag::limit[]
      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().skip(3).limit(4));
      // end::limit[]
    }

    {
      // tag::ryow[]
      MutationResult mutationResult = collection.upsert("key", JsonObject.create().put("description", "swanky"));
      MutationState mutationState = MutationState.from(mutationResult.mutationToken().get());

      SearchResult searchResult = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().consistentWith(mutationState));
      // end::ryow[]
    }

    {
      // tag::highlight[]
      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().highlight(HighlightStyle.HTML, "description", "type"));
      // end::highlight[]
    }

    {
      // tag::sort[]
      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().sort(SearchSort.byScore(), SearchSort.byField("description")));
      // end::sort[]
    }

    {
      // tag::facets[]
      Map<String, SearchFacet> facets = new HashMap<>();
      facets.put("types", SearchFacet.term("type", 5));

      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("United States"),
          searchOptions().facets(facets));
      // end::facets[]
    }

    {
      // tag::fields[]
      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().fields("description", "type"));
      // end::fields[]
    }

    {
      // tag::collections[]
      SearchResult result = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("San Francisco"),
          searchOptions().collections("landmark", "airport"));
      // end::collections[]
    }

    {
      // tag::simplereactive[]
      Mono<ReactiveSearchResult> result = cluster.reactive().searchQuery("travel-sample-index",
          SearchQuery.queryString("swanky"));

      result.flatMapMany(ReactiveSearchResult::rows).subscribe(row -> System.out.println("Found reactive row: " + row));
      // end::simplereactive[]
    }

    {
      // tag::backpressure[]
      Mono<ReactiveSearchResult> result = cluster.reactive().searchQuery("travel-sample-index",
          SearchQuery.queryString("swanky"));

      result.flatMapMany(ReactiveSearchResult::rows).subscribe(new BaseSubscriber<SearchRow>() {
        // Number of outstanding requests
        final AtomicInteger oustanding = new AtomicInteger(0);

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
          request(10); // initially request to rows
          oustanding.set(10);
        }

        @Override
        protected void hookOnNext(SearchRow row) {
          process(row);
          if (oustanding.decrementAndGet() == 0) {
            request(10);
          }
        }
      });
      // end::backpressure[]
    }
  }

  static void process(SearchRow value) {

  }

}
