/*
 * Copyright (c) 2022 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.time.Duration;
import java.util.Arrays;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.DropPrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.CreateQueryIndexOptions;
import com.couchbase.client.java.manager.query.WatchQueryIndexesOptions;

public class IndexHelloWorld {

  public static void main(String... args) {
    Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

    {
      System.out.println("[primary]");
      // tag::primary[]
      CreatePrimaryQueryIndexOptions opts = CreatePrimaryQueryIndexOptions
          .createPrimaryQueryIndexOptions()
          .ignoreIfExists(true);

      cluster.queryIndexes().createPrimaryIndex("travel-sample", opts);
      // end::primary[]
      System.out.println("Index creation complete");
    }

    {
      System.out.println("\n[named-primary]");
      // tag::named-primary[]
      CreatePrimaryQueryIndexOptions opts = CreatePrimaryQueryIndexOptions
          .createPrimaryQueryIndexOptions()
          .indexName("named_primary_index");

      cluster.queryIndexes().createPrimaryIndex("travel-sample", opts);
      // end::named-primary[]
      System.out.println("Named primary index creation complete");
    }

    {
      System.out.println("\n[secondary]");
      // tag::secondary[]
      cluster.queryIndexes().createIndex(
        "travel-sample", 
        "index_name",
        Arrays.asList("name")
      );
      // end::secondary[]
      System.out.println("Index creation complete");
    }

    {
      System.out.println("\n[composite]");
      // tag::composite[]
      cluster.queryIndexes().createIndex(
        "travel-sample", 
        "index_travel_info", 
        Arrays.asList("name", "id", "icao", "iata")
      );
      // end::composite[]
      System.out.println("Index creation complete");
    }

    {
      System.out.println("\n[drop-primary]");
      // tag::drop-primary[]
      cluster.queryIndexes().dropPrimaryIndex("travel-sample");
      // end::drop-primary[]
      System.out.println("Primary index deleted successfully");
    }

    {
      System.out.println("\n[drop-secondary]");
      // tag::drop-secondary[]
      cluster.queryIndexes().dropIndex("travel-sample", "index_name");
      // end::drop-secondary[]
      System.out.println("Index deleted successfully");
    }

    {
      System.out.println("\n[defer-create]");
      // tag::defer-create-primary[]
      CreatePrimaryQueryIndexOptions primaryOpts = CreatePrimaryQueryIndexOptions
          .createPrimaryQueryIndexOptions()
          .deferred(true);

      cluster.queryIndexes().createPrimaryIndex("travel-sample", primaryOpts);
      // end::defer-create-primary[]

      // tag::defer-create-secondary[]
      CreateQueryIndexOptions secondaryOpts = CreateQueryIndexOptions
          .createQueryIndexOptions()
          .deferred(true);

      cluster.queryIndexes().createIndex(
        "travel-sample", 
        "idx_name_email",	
        Arrays.asList("name", "email"), 
        secondaryOpts
      );
      // end::defer-create-secondary[]
      System.out.println("Created deferred indexes");
    }

    {
      System.out.println("\n[defer-build]");
      // tag::defer-build[]
      // Start building any deferred indexes which were previously created.
      cluster.queryIndexes().buildDeferredIndexes("travel-sample");

      WatchQueryIndexesOptions opts = WatchQueryIndexesOptions
          .watchQueryIndexesOptions()
          .watchPrimary(true);

      // Wait for the deferred indexes to be ready for use.
      // Set the maximum time to wait to 3 minutes.
      cluster.queryIndexes().watchIndexes(
        "travel-sample", 
        Arrays.asList("idx_name_email"), 
        Duration.ofMinutes(3), 
        opts
      );
      // end::defer-build[]
      System.out.println("Deferred indexes ready");
    }
  }
}
