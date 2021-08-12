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

import static com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.couchbase.client.core.deps.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

public class CloudConnect {

    public static void main(String... args) {
        // tag::cloud-connect[]
        // Update these variables to point to your Cloud instance and credentials.
        String endpoint = "--your-instance--.dp.cloud.couchbase.com";
        String bucketname = "bucketname";
        String username = "username";
        String password = "password";

        String tlsCertificate = "-----BEGIN CERTIFICATE-----\n" +
          "... your certificate content in here ...\n" +
          "-----END CERTIFICATE-----";
        // User Input ends here.

        // Configure TLS
        List<X509Certificate> cert = SecurityConfig.decodeCertificates(Collections.singletonList(tlsCertificate));
        SecurityConfig.Builder securityConfig = SecurityConfig
          .enableTls(true) // Enable transport security
          .trustCertificates(cert); // Configure the cloud certificate
          // During development, if you want to trust all certificates you can connect
          // to your cloud like with the InsecureTrustManagerFactory. As the name points
          // out, this is INSECURE!
          // .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE)

        // Build the environment with the TLS config
        ClusterEnvironment env = ClusterEnvironment
          .builder()
          .securityConfig(securityConfig)
          .build();

        // Initialize the Connection
        Cluster cluster = Cluster.connect(endpoint, ClusterOptions.clusterOptions(username, password).environment(env));
        Bucket bucket = cluster.bucket(bucketname);
        bucket.waitUntilReady(Duration.parse("PT10S")) ;
        Collection collection = bucket.defaultCollection();

        // Create a JSON Document
        JsonObject arthur = JsonObject.create()
          .put("name", "Arthur")
          .put("email", "kingarthur@couchbase.com")
          .put("interests", JsonArray.from("Holy Grail", "African Swallows"));

        // Store the Document
        collection.upsert("u:king_arthur", arthur);

        // Load the Document and print it
        // Prints Content and Metadata of the stored Document
        System.out.println(collection.get("u:king_arthur"));

        // Create a N1QL Primary Index
        cluster.queryIndexes().createPrimaryIndex(
          bucketname,
          createPrimaryQueryIndexOptions().ignoreIfExists(true)
        );

        // Perform a N1QL Query
        QueryResult result = cluster.query(
          "SELECT name FROM " + bucketname + " WHERE $1 IN interests",
          queryOptions()
            .parameters(JsonArray.from("African Swallows"))
            .scanConsistency(QueryScanConsistency.REQUEST_PLUS)
        );

        // Print each found Row
        for (JsonObject row : result.rowsAsObject()) {
            System.out.println("Query row: " + row);
        }
        // end::cloud-connect[]
    }
}
