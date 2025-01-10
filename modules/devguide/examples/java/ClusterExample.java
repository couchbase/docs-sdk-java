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
Extension of managing_connections
file: howtos/pages/managing-connections.adoc line: 277
 */

import java.time.Duration;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;

// tag::managing_connections_9[]
public class ClusterExample {
    public static void main(String... args) throws Exception {
      Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
      cluster.waitUntilReady(Duration.ofSeconds(10));
      Bucket bucket = cluster.bucket("travel-sample");
      Collection collection = bucket.defaultCollection();
    }
  }
// end::managing_connections_9[]