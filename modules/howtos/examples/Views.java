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

import static com.couchbase.client.java.view.ViewOptions.viewOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.codec.TypeRef;
import com.couchbase.client.java.manager.view.DesignDocument;
import com.couchbase.client.java.manager.view.View;
import com.couchbase.client.java.manager.view.ViewIndexManager;
import com.couchbase.client.java.view.DesignDocumentNamespace;
import com.couchbase.client.java.view.ViewMetaData;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.client.java.view.ViewScanConsistency;

public class Views {

  public static void main(String... args) {

    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
    Bucket bucket = cluster.bucket("travel-sample");

    // Create a View
    createView(bucket);

    {
      // tag::views-simple[]
      ViewResult viewResult = bucket.viewQuery("dev_landmarks-by-name", "by_name");
      for (ViewRow row : viewResult.rows()) {
        System.out.println("Found row: " + row);
      }
      // end::views-simple[]
    }

    {
      // tag::views-dev[]
      ViewResult viewResult = bucket.viewQuery("landmarks-by-name", "by_name",
          viewOptions().namespace(DesignDocumentNamespace.DEVELOPMENT));
      // end::views-dev[]
    }

    {
      // tag::views-opts[]
      ViewResult viewResult = bucket.viewQuery("dev_landmarks-by-name", "by_name",
          viewOptions().scanConsistency(ViewScanConsistency.REQUEST_PLUS).limit(5).inclusiveEnd(true));
      // end::views-opts[]
    }

    {
      // tag::views-meta[]
      ViewResult viewResult = bucket.viewQuery("dev_landmarks-by-name", "by_name", viewOptions().debug(true));

      ViewMetaData viewMeta = viewResult.metaData();
      System.out.println("Got total rows: " + viewMeta.totalRows());
      viewMeta.debug().ifPresent(debug -> System.out.println("Got debug info as well: " + debug));
      // end::views-meta[]
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

  // tag::view_rows_structure[]
  public class ViewRowExample {

    public Optional<String> id() {
      Optional<String> result = null;
      /*  */ return result;
    }

    public <T> Optional<T> keyAs(Class<T> target) {
      Optional<T> result = null;
      /*  */ return result;
    }

    public <T> Optional<T> keyAs(TypeRef<T> target) {
      Optional<T> result = null;
      /*  */ return result;
    }

    public <T> Optional<T> valueAs(Class<T> target) {
      Optional<T> result = null;
      /*  */ return result;
    }

    public <T> Optional<T> valueAs(TypeRef<T> target) {
      Optional<T> result = null;
      /*  */ return result;
    }
  }
  // end::view_rows_structure[]

}
