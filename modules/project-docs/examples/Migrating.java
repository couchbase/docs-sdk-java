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

import static com.couchbase.client.java.ClusterOptions.clusterOptions;
import static com.couchbase.client.java.analytics.AnalyticsOptions.analyticsOptions;
import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.kv.UpsertOptions.upsertOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static com.couchbase.client.java.search.SearchOptions.searchOptions;
import static com.couchbase.client.java.view.ViewOptions.viewOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;

import com.couchbase.client.core.env.CertificateAuthenticator;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.InvalidArgumentException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.analytics.AnalyticsResult;
import com.couchbase.client.java.codec.RawJsonTranscoder;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.view.DesignDocument;
import com.couchbase.client.java.manager.view.View;
import com.couchbase.client.java.manager.view.ViewIndexManager;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.search.SearchMetaData;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.result.SearchResult;
import com.couchbase.client.java.search.result.SearchRow;
import com.couchbase.client.java.view.DesignDocumentNamespace;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;

// This example assumes an index called `travel-sample-index` exists.
// Please refer to file `modules/test/scripts/init-couchbase/init-buckets.sh` (line 66)
// for the relevant curl command to create this.

public class Migrating {
  public static void main(String... args) {
    {
      // tag::timeoutbuilder[]
      // SDK 3 equivalent
      ClusterEnvironment env = ClusterEnvironment.builder()
          .timeoutConfig(TimeoutConfig.kvTimeout(Duration.ofSeconds(5))).build();
      // end::timeoutbuilder[]
    }

    {
      // tag::shutdown[]
      ClusterEnvironment env = ClusterEnvironment.create();
      Cluster cluster = Cluster.connect("127.0.0.1",
          // pass the custom environment through the cluster options
          clusterOptions("Administrator", "password").environment(env));

      // first disconnect, then shutdown the environment
      cluster.disconnect();
      env.shutdown();
      // end::shutdown[]
    }

    {
      // tag::sysprops[]
      // Will set the max http connections to 23
      System.setProperty("com.couchbase.env.io.maxHttpConnections", "23");
      Cluster.connect("127.0.0.1", "Administrator", "password");

      // This is equivalent to
      ClusterEnvironment env = ClusterEnvironment.builder().ioConfig(IoConfig.maxHttpConnections(23)).build();
      // end::sysprops[]
    }

    {
      // tag::connstr[]
      // Will set the max http connections to 23
      Cluster.connect("127.0.0.1?io.maxHttpConnections=23", "Administrator", "password");

      // This is equivalent to
      ClusterEnvironment env = ClusterEnvironment.builder().ioConfig(IoConfig.maxHttpConnections(23)).build();
      // end::connstr[]
    }

    {
      // tag::rbac[]
      Cluster.connect("127.0.0.1", "Administrator", "password");
      // end::rbac[]
    }

    {
      // tag::rbac-full[]
      Cluster.connect("127.0.0.1", clusterOptions(PasswordAuthenticator.create("Administrator", "password")));
      // end::rbac-full[]
    }

    {
      try {
        // tag::certauth[]
        KeyManagerFactory keyManagerFactory = null; // configure certificates per documentation
        Cluster.connect("127.0.0.1",
            clusterOptions(CertificateAuthenticator.fromKeyManagerFactory(() -> keyManagerFactory)));
        // end::certauth[]
      } catch (InvalidArgumentException e) {
        // The code requires certificates to be configured, catching the exception for
        // example purposes only.
      }
    }

    {
      // tag::simpleget[]
      Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
      Bucket bucket = cluster.bucket("travel-sample");
      Scope scope = bucket.scope("inventory");
      Collection collection = scope.collection("airline");

      GetResult getResult = collection.get("airline_10");

      cluster.disconnect();
      // end::simpleget[]
    }

    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
    Bucket bucket = cluster.bucket("travel-sample");
    Scope scope = bucket.scope("inventory");
    Collection collection = scope.collection("airport");

    {
      // tag::upsertandget[]
      MutationResult upsertResult = collection.upsert("mydoc-id", JsonObject.create());
      GetResult getResult = collection.get("mydoc-id");
      // end::upsertandget[]
    }

    {
      // tag::rawjson[]
      byte[] content = "{}".getBytes(StandardCharsets.UTF_8);
      MutationResult upsertResult = collection.upsert("mydoc-id", content,
          upsertOptions().transcoder(RawJsonTranscoder.INSTANCE));
      // end::rawjson[]
    }

    {
      // tag::customtimeout[]
      // SDK 3 custom timeout
      GetResult getResult = collection.get("mydoc-id", getOptions().timeout(Duration.ofSeconds(5)));
      // end::customtimeout[]
    }

    {
      // tag::querysimple[]
      // SDK 3 simple query
      QueryResult queryResult = cluster.query("select * from `travel-sample`.inventory.airport limit 10");
      for (JsonObject value : queryResult.rowsAsObject()) {
        // ...
      }
      // end::querysimple[]
    }

    {
      // tag::queryparameterized[]
      // SDK 3 named parameters
      cluster.query("select * from `travel-sample`.inventory.airport where airportname = $name",
          queryOptions().parameters(JsonObject.create().put("name", "Calais Dunkerque")));

      // SDK 3 positional parameters
      cluster.query("select * from `travel-sample`.inventory.airport where airportname = $1",
          queryOptions().parameters(JsonArray.from("Calais Dunkerque")));
      // end::queryparameterized[]
    }

    {
      // tag::analyticssimple[]
      // SDK 3 simple analytics query
      AnalyticsResult analyticsResult = cluster.analyticsQuery("select * from `travel-sample`.inventory.airport limit 10");
      for (JsonObject value : analyticsResult.rowsAsObject()) {
        // ...
      }
      // end::analyticssimple[]
    }

    {
      // tag::analyticsparameterized[]
      // SDK 3 named parameters for analytics
      cluster.analyticsQuery("select * from `travel-sample`.inventory.airport where airportname = $name",
          analyticsOptions().parameters(JsonObject.create().put("name", "Calais Dunkerque")));

      // SDK 3 positional parameters for analytics
      cluster.analyticsQuery("select * from `travel-sample`.inventory.airport where airportname = $1",
          analyticsOptions().parameters(JsonArray.from("Calais Dunkerque")));
      // end::analyticsparameterized[]
    }

    {
      // tag::searchsimple[]
      // SDK 3 search query
      SearchResult searchResult = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"),
          searchOptions().timeout(Duration.ofSeconds(2)).limit(5).fields("description", "type", "city"));
      for (SearchRow row : searchResult.rows()) {
        // ...
      }
      // end::searchsimple[]
    }

    {
      // tag::searchcheck[]
      SearchResult searchResult = cluster.searchQuery("travel-sample-index", SearchQuery.queryString("swanky"));
      SearchMetaData searchMetaData = searchResult.metaData();
      if (searchMetaData.errors() == null || searchMetaData.errors().isEmpty()) {
        // no errors present, so full data got returned
      }
      // end::searchcheck[]
    }

    {
      // Create a View
      createView(bucket);

      // tag::viewquery[]
      // SDK 3 view query
      ViewResult viewResult = bucket.viewQuery("dev_landmarks-by-name", "by_name",
          viewOptions().limit(5).skip(2).timeout(Duration.ofSeconds(10)));
      for (ViewRow row : viewResult.rows()) {
        // ...
      }
      // end::viewquery[]
    }
  }

  private static void createView(Bucket bucket) {
    ViewIndexManager viewMgr = bucket.viewIndexes();
    View view = new View("function (doc, meta) { if (doc.type == 'landmark') { emit(doc.name, null); } }");

    Map<String, View> views = new HashMap<>();
    views.put("by_name", view);

    // Create Design Doc
    DesignDocument designDocument = new DesignDocument("landmarks-by-name", views);

    // Upsert Design Doc
    viewMgr.upsertDesignDocument(designDocument, DesignDocumentNamespace.DEVELOPMENT);
  }
}
