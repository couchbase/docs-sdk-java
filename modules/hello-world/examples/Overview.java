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

import static com.couchbase.client.java.kv.MutateInOptions.mutateInOptions;

import java.util.Collections;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutateInSpec;
import com.couchbase.client.java.kv.MutationResult;

public class Overview {

    static String connectionString = "localhost";
    static String username = "Administrator";
    static String password = "password";
    static String bucketName = "travel-sample";

    public static void main(String... args) {
        Cluster cluster = Cluster.connect(connectionString, username, password);
        Bucket bucket = cluster.bucket(bucketName);
        // tag::overview[]
        Scope scope = bucket.scope("inventory");
        Collection collection = scope.collection("airport");
        MutationResult result = collection.mutateIn(
                "airport_1254",
                Collections.singletonList(MutateInSpec.upsert("foo", "bar")),
                mutateInOptions().durability(DurabilityLevel.MAJORITY)
        );
        // end::overview[]
        GetResult getResult = collection.get("airport_1254");
        String foo = getResult.contentAsObject().getString("foo");
        System.out.println("RESULT: " + foo); // foo == "bar"
    }

}
