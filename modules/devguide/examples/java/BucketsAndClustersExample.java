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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;

public class BucketsAndClustersExample {

  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";
  String bucketName = "travel-sample";

  Cluster cluster;
  Bucket bucket;
  BucketSettings bucketSettings;

  private void init() {
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
  }

  public void buckets_and_clusters_1() throws Exception {
    // tag::buckets_and_clusters_1[]
    BucketManager manager = cluster.buckets();
    bucketSettings = BucketSettings.create("myBucket");
    manager.createBucket(bucketSettings);
    // end::buckets_and_clusters_1[]
  }

  public static void main(String[] args) throws Exception {
    BucketsAndClustersExample obj = new BucketsAndClustersExample();
    obj.init();
    obj.buckets_and_clusters_1();
    System.out.println("Done.");
  }
}
