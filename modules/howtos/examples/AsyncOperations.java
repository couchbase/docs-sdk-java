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

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.AsyncCluster;
import com.couchbase.client.java.AsyncCollection;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveBucket;
import com.couchbase.client.java.ReactiveCluster;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import io.reactivex.Single;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

public class AsyncOperations {

  public static void main(String... args) {

// #tag::access[]
Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
ReactiveCluster reactiveCluster = cluster.reactive();

Bucket bucket = cluster.bucket("travel-sample");
ReactiveBucket reactiveBucket = bucket.reactive();

Collection collection = bucket.defaultCollection();
ReactiveCollection reactiveCollection = collection.reactive();
// #end::access[]

// #tag::access-async[]
AsyncCluster asyncCluster = cluster.async();
AsyncBucket asyncBucket = bucket.async();
AsyncCollection asyncCollection = collection.async();
// #end::access-async[]


// #tag::simple-get[]
reactiveCollection
  .get("my-doc")
  .subscribe(System.out::println, System.err::println);
// #end::simple-get[]

// #tag::non-used-upsert[]
reactiveCollection.upsert("my-doc", JsonObject.create());
// #end::non-used-upsert[]

// #tag::verbose-query[]
reactiveCluster
  .query("select * from `travel-sample`")
  .flux()
  .flatMap(result -> {
    Flux<JsonObject> rows = result.rowsAs(JsonObject.class);
    return rows;
  }).subscribe(row -> {
    System.out.println("Found row: " + row);
  });
// #end::verbose-query[]

    {
// #tag::simple-bulk[]
List<String> docsToFetch = Arrays.asList("key1", "key2", "key3");
List<GetResult> results = Flux
  .fromIterable(docsToFetch)
  .flatMap(reactiveCollection::get)
  .collectList()
  .block();
// #end::simple-bulk[]
    }

    {
// #tag::ignore-bulk[]
List<String> docsToFetch = Arrays.asList("key1", "key2", "key3");
List<GetResult> results = Flux
  .fromIterable(docsToFetch)
  .flatMap(key -> reactiveCollection.get(key).onErrorResume(e -> Mono.empty()))
  .collectList()
  .block();
// #end::ignore-bulk[]
    }


    {
// #tag::split-bulk[]
List<String> docsToFetch = Arrays.asList("key1", "key2", "key3");

List<GetResult> successfulResults =
  Collections.synchronizedList(new ArrayList<>());
Map<String, Throwable> erroredResults =
  new ConcurrentHashMap<>();

Flux
  .fromIterable(docsToFetch)
  .flatMap(key -> reactiveCollection.get(key).onErrorResume(e -> {
    erroredResults.put(key, e);
    return Mono.empty();
  }))
  .doOnNext(successfulResults::add)
  .last()
  .block();
// #end::split-bulk[]
    }


    {
// #tag::retry-bulk[]
List<String> docsToFetch = Arrays.asList("key1", "key2", "key3");

List<GetResult> results = Flux
  .fromIterable(docsToFetch)
  .flatMap(key -> reactiveCollection
    .get(key)
    .retryBackoff(10, Duration.ofMillis(10)))
  .collectList()
  .block();
// #end::retry-bulk[]
    }

// #tag::rs-conversion[]
Single<GetResult> rxSingleResult = monoToSingle(reactiveCollection.get("my-doc"));
// #end::rs-conversion[]

  }
}
