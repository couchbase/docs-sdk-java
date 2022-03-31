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

// tag::imports[]

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.transactions.TransactionKeyspace;
import com.couchbase.client.java.transactions.config.TransactionsCleanupConfig;
import com.couchbase.client.java.transactions.config.TransactionsConfig;
import com.couchbase.client.java.transactions.error.TransactionCommitAmbiguousException;
import com.couchbase.client.java.transactions.error.TransactionFailedException;

import java.time.Duration;
import java.util.logging.Logger;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static com.couchbase.client.java.transactions.config.SingleQueryTransactionOptions.singleQueryTransactionOptions;

// end::imports[]

public class TransactionsMigration {
    static Logger logger = Logger.getLogger(TransactionsMigration.class.getName());
    static Cluster cluster;
    static Collection collection;

    public static void main(String... args) {
        // tag::init[]
        // Initialize the Couchbase cluster
        Cluster cluster = Cluster.connect("localhost", "username", "password");
        Bucket bucket = cluster.bucket("travel-sample");
        Scope scope = bucket.scope("inventory");
        Collection collection = scope.collection("airport");
        // end::init[]

        TransactionsMigration.cluster = cluster;
        TransactionsMigration.collection = collection;
    }

    static void access() {
        // tag::access[]
        cluster.transactions().run((ctx) -> {
            // Your transaction logic.
        });
        // end::access[]
    }

    static void config() {
        // tag::config[]
        var keyspace = TransactionKeyspace.create("bucketName", "scopeName", "collectionName");

        var cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig
                        .durabilityLevel(DurabilityLevel.PERSIST_TO_MAJORITY)
                        .metadataCollection(keyspace))));
        // end::config[]
    }

    static void singleQuery() {
        // tag::single-query[]
        QueryResult qr = cluster.query("INSERT...", queryOptions().asTransaction());
        // end::single-query[]
    }

    static void singleQueryConfig() {
        // tag::single-query-config[]
        QueryResult qr = cluster.query("INSERT...",
                queryOptions().asTransaction(
                        singleQueryTransactionOptions()
                                .durabilityLevel(DurabilityLevel.MAJORITY)));
        // end::single-query-config[]
    }

    static void configCleanup() {
        // tag::config-cleanup[]
        var cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig
                                .cleanupConfig(TransactionsCleanupConfig
                                        .cleanupClientAttempts(true)
                                        .cleanupLostAttempts(true)
                                        .cleanupWindow(Duration.ofSeconds(30))))));
        // end::config-cleanup[]
    }

    static void log() {
        try {
            cluster.transactions().run((ctx) -> {
                // Your transaction logic.
            });
        }
        catch (TransactionCommitAmbiguousException err) {
            logger.warning("Transaction possibly committed:");
            err.logs().forEach(msg -> logger.warning(msg.toString()));
        }
        // tag::log[]
        catch (TransactionFailedException err) {
            err.logs().forEach(msg -> logger.warning(msg.toString()));
        }
        // end::log[]
    }
}
