/*
 * Copyright (c) 2024 Couchbase, Inc.
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

// tag::imports[]
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.env.SecurityConfig;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.ReplaceOptions;

import java.time.Duration;
import java.util.UUID;
// end::imports[]

public class Cloud {
    public static void main(String[] args) {
        // tag::connect[]
        // Update this to your cluster
        String endpoint = "cb.<your-endpoint>.cloud.couchbase.com";
        String username = "username";
        String password = "Password!123";
        String bucketName = "travel-sample";

        ClusterEnvironment env = ClusterEnvironment.builder()
            .securityConfig(SecurityConfig.enableTls(true))
            // Sets a pre-configured profile called "wan-development" to help avoid latency issues
            // when accessing Capella from a different Wide Area Network
            // or Availability Zone (e.g. your laptop).
            .applyProfile(ClusterEnvironment.WanDevelopmentProfile.INSTANCE)
            .build();

        Cluster cluster = Cluster.connect(
            "couchbases://" + endpoint,
            ClusterOptions.clusterOptions(username, password).environment(env)
        );
        // end::connect[]

        // tag::bucket[]
        var bucket = cluster.bucket(bucketName);
        bucket.waitUntilReady(Duration.ofSeconds(30));
        // end::bucket[]

        // tag::collection[]
        var collection = bucket.scope("inventory").collection("airport");
        // end::collection[]

        // tag::json[]
        JsonObject json = JsonObject.create().put("status", "awesome");
        // end::json[]

        // tag::upsert[]
        String docId = UUID.randomUUID().toString();
        try {
            collection.upsert(docId, json);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // end::upsert[]

        // tag::get[]
        // Get a document
        try {
            GetResult result = collection.get(docId);
            JsonObject content = result.contentAsObject();
            String status = content.getString("status");
            System.out.println("Couchbase is " + status);
        } catch (Exception e) {
            System.err.println("Error getting document: " + e.getMessage());
        }
        // end::get[]

        // tag::get-for[]
        try {
            String status = collection.get(docId)
                .contentAsObject()
                .getString("status");
            System.out.println("Couchbase is " + status);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // end::get-for[]

        // tag::replace-options[]
        try {
            collection.replace(
                docId,
                json,
                ReplaceOptions.replaceOptions()
                    .expiry(Duration.ofSeconds(10))
                    .durability(com.couchbase.client.java.kv.Durability.MAJORITY)
            );
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // end::replace-options[]

        // tag::replace-named[]
        try {
            collection.replace(
                docId,
                json,
                ReplaceOptions.replaceOptions()
                    .durability(com.couchbase.client.java.kv.Durability.MAJORITY)
            );
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // end::replace-named[]
    }
}
