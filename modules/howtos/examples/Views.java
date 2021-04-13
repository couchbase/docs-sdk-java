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

import java.util.Optional;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.codec.TypeRef;
import com.couchbase.client.java.view.DesignDocumentNamespace;
import com.couchbase.client.java.view.ViewMetaData;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.client.java.view.ViewScanConsistency;

public class Views {

  public static void main(String... args) {

    {
      // tag::views-simple[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      Bucket bucket = cluster.bucket("bucket-name");

      ViewResult viewResult = bucket.viewQuery("design-doc-name", "view-name");
      for (ViewRow row : viewResult.rows()) {
        System.out.println("Found row: " + row);
      }
      // end::views-simple[]
    }

    Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
    Bucket bucket = cluster.bucket("bucket-name");

    {
      // tag::views-dev[]
      ViewResult viewResult = bucket.viewQuery("ddoc", "view",
          viewOptions().namespace(DesignDocumentNamespace.DEVELOPMENT));
      // end::views-dev[]
    }

    {
      // tag::views-opts[]
      ViewResult viewResult = bucket.viewQuery("ddoc", "view",
          viewOptions().scanConsistency(ViewScanConsistency.REQUEST_PLUS).limit(5).inclusiveEnd(true));
      // end::views-opts[]
    }

    {
      // tag::views-meta[]
      ViewResult viewResult = bucket.viewQuery("ddoc", "view", viewOptions().debug(true));

      ViewMetaData viewMeta = viewResult.metaData();
      System.out.println("Got total rows: " + viewMeta.totalRows());
      viewMeta.debug().ifPresent(debug -> System.out.println("Got debug info as well: " + debug));
      // end::views-meta[]
    }

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
