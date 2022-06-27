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

import com.couchbase.client.core.cnc.events.transaction.TransactionLogEvent;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.core.transactions.events.IllegalDocumentStateEvent;
import com.couchbase.client.core.transactions.events.TransactionCleanupAttemptEvent;
import com.couchbase.client.core.transactions.events.TransactionCleanupEndRunEvent;
import com.couchbase.client.core.transactions.events.TransactionEvent;
import com.couchbase.client.core.transactions.log.CoreTransactionLogMessage;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveScope;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryProfile;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.transactions.*;
import com.couchbase.client.java.transactions.config.TransactionsCleanupConfig;
import com.couchbase.client.java.transactions.config.TransactionsConfig;
import com.couchbase.client.java.transactions.config.TransactionsQueryConfig;
import com.couchbase.client.java.transactions.error.TransactionCommitAmbiguousException;
import com.couchbase.client.java.transactions.error.TransactionFailedException;
import com.couchbase.client.java.transactions.log.TransactionLogMessage;
import com.couchbase.client.tracing.opentelemetry.OpenTelemetryRequestSpan;
import io.opentelemetry.api.trace.Span;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.couchbase.client.java.transactions.config.SingleQueryTransactionOptions.singleQueryTransactionOptions;
import static com.couchbase.client.java.transactions.config.TransactionOptions.transactionOptions;

// end::imports[]

public class TransactionsExample {
    static Logger logger = Logger.getLogger(TransactionsExample.class.getName());
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

        TransactionsExample.cluster = cluster;
        TransactionsExample.collection = collection;

        queryExamples();
    }

    static void config() {
        // tag::config[]
        ClusterEnvironment env = ClusterEnvironment.builder()
                .transactionsConfig(TransactionsConfig.durabilityLevel(DurabilityLevel.PERSIST_TO_MAJORITY)
                        .cleanupConfig(TransactionsCleanupConfig.cleanupWindow(Duration.ofSeconds(30)))
                        .queryConfig(TransactionsQueryConfig.scanConsistency(QueryScanConsistency.NOT_BOUNDED)))
                .build();

        Cluster cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env));

        // Use the cluster
        // ...

        // Shutdown
        cluster.disconnect();
        env.shutdown();
        // end::config[]
    }

    static void configEasy() {
        // tag::config-easy[]
        Cluster cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig
                        .durabilityLevel(DurabilityLevel.PERSIST_TO_MAJORITY)
                        .cleanupConfig(TransactionsCleanupConfig
                                .cleanupWindow(Duration.ofSeconds(30)))
                        .queryConfig(TransactionsQueryConfig
                                .scanConsistency(QueryScanConsistency.NOT_BOUNDED)))));
        // Use the cluster
        // ...

        // Shutdown
        cluster.disconnect();
        // end::config-easy[]
    }

    static void createSimple() {
        JsonObject doc1Content = JsonObject.create();
        JsonObject doc2Content = JsonObject.create();

        // tag::create-simple[]
        cluster.transactions().run((ctx) -> {
            ctx.insert(collection, "doc1", doc1Content);

            var doc2 = ctx.get(collection, "doc2");
            ctx.replace(doc2, doc2Content);
        });
        // end::create-simple[]
    }

    static void create() {
        // tag::create[]
        try {
            cluster.transactions().run((ctx) -> {
                // 'ctx' is a TransactionAttemptContext, which permits getting, inserting,
                // removing and replacing documents, performing N1QL queries, and committing or
                // rolling back the transaction.

                // ... Your transaction logic here ...

                // If the lambda succeeds, the transaction is automatically committed.
            });
            // tag::logging[]
        } catch (TransactionCommitAmbiguousException e) {
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            throw logFailure(e);
        }
        // end::logging[]
        // end::create[]
    }

    static void loggingSucccess() {
        // tag::logging-success[]
        TransactionResult result = cluster.transactions().run((ctx) -> {
            // Your transaction logic
        });

        result.logs().forEach(message -> logger.info(message.toString()));
        // end::logging-success[]
    }

    static void createReactive() {
        // tag::createReactive[]
        Mono<TransactionResult> result = cluster.reactive().transactions().run((ctx) -> {
            // 'ctx' is a TransactionAttemptContextReactive, providing asynchronous versions of the
            // TransactionAttemptContext methods.

            // Your transaction logic here: as an example, get and remove a doc
            return ctx.get(collection.reactive(), "document-id")

                    .flatMap(doc -> ctx.remove(doc));
            // tag::async_logging[]
        }).doOnError(err -> {
            if (err instanceof TransactionCommitAmbiguousException) {
                throw logCommitAmbiguousError((TransactionCommitAmbiguousException) err);
            } else if (err instanceof TransactionFailedException) {
                throw logFailure((TransactionFailedException) err);
            }
        });
        // end::async_logging[]

        // Normally you will chain this result further and ultimately subscribe. For
        // simplicity, here we just block on the result.
        TransactionResult finalResult = result.block();
        // end::createReactive[]
    }

    static void examples() {
        // tag::examples[]
        Scope inventory = cluster.bucket("travel-sample").scope("inventory");

        try {
            TransactionResult result = cluster.transactions().run((ctx) -> {
                // Inserting a doc:
                ctx.insert(collection, "doc-a", JsonObject.create());

                // Getting documents:
                TransactionGetResult docA = ctx.get(collection, "doc-a");

                // Replacing a doc:
                TransactionGetResult docB = ctx.get(collection, "doc-b");
                JsonObject content = docB.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                ctx.replace(docB, content);

                // Removing a doc:
                TransactionGetResult docC = ctx.get(collection, "doc-c");
                ctx.remove(docC);

                // Performing a SELECT N1QL query against a scope:
                TransactionQueryResult qr = ctx.query(inventory, "SELECT * FROM hotel WHERE country = $1",
                        TransactionQueryOptions.queryOptions()
                                .parameters(JsonArray.from("United Kingdom")));
                var rows = qr.rowsAs(JsonObject.class);

                // Performing an UPDATE N1QL query on multiple documents, in the `inventory` scope:
                ctx.query(inventory, "UPDATE route SET airlineid = $1 WHERE airline = $2",
                        TransactionQueryOptions.queryOptions()
                                .parameters(JsonArray.from("airline_137", "AF")));
            });
        } catch (TransactionCommitAmbiguousException e) {
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            throw logFailure(e);
        }
        // end::examples[]
    }

    static void examplesReactive() {
        // tag::examplesReactive[]
        ReactiveScope inventory = cluster.bucket("travel-sample").scope("inventory").reactive();

        Mono<TransactionResult> result = cluster.reactive().transactions().run((ctx) -> {
            // Inserting a doc:
            return ctx.insert(collection.reactive(), "doc-a", JsonObject.create())

                    // Getting and replacing a doc:
                    .then(ctx.get(collection.reactive(), "doc-b")).flatMap(docB -> {
                        var content = docB.contentAs(JsonObject.class);
                        content.put("transactions", "are awesome");
                        return ctx.replace(docB, content);
                    })

                    // Getting and removing a doc:
                    .then(ctx.get(collection.reactive(), "doc-c"))
                        .flatMap(doc -> ctx.remove(doc))

                    // Performing a SELECT N1QL query, in the `inventory` scope:
                    .then(ctx.query(inventory, "SELECT * FROM hotel WHERE country = $1",
                            TransactionQueryOptions.queryOptions()
                                    .parameters(JsonArray.from("United Kingdom"))))

                    .flatMap(queryResult -> {
                        var rows = queryResult.rowsAs(JsonObject.class);
                        // The application would do something with the rows here.
                        return Mono.empty();
                    })

                    // Performing an UPDATE N1QL query on multiple documents, in the `inventory` scope:
                    .then(ctx.query(inventory, "UPDATE route SET airlineid = $1 WHERE airline = $2",
                            TransactionQueryOptions.queryOptions()
                                    .parameters(JsonArray.from("airline_137", "AF"))));
        }).doOnError(err -> {
            if (err instanceof TransactionCommitAmbiguousException) {
                throw logCommitAmbiguousError((TransactionCommitAmbiguousException) err);
            } else if (err instanceof TransactionFailedException) {
                throw logFailure((TransactionFailedException) err);
            }
        });

        // Normally you will chain this result further and ultimately subscribe.
        // For simplicity, here we just block on the result.
        result.block();
        // end::examplesReactive[]

    }

    static void insert() {
        // tag::insert[]
        cluster.transactions().run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());
        });
        // end::insert[]
    }

    static void insertReactive() {
        // tag::insertReactive[]
        cluster.reactive().transactions().run((ctx) -> {
            return ctx.insert(collection.reactive(), "docId", JsonObject.create());
        }).block();
        // end::insertReactive[]
    }

    static void getCatch() {
        // tag::get-catch[]
        cluster.transactions().run((ctx) -> {
            try {
                TransactionGetResult doc = ctx.get(collection, "a-doc");
            }
            catch (DocumentNotFoundException err) {
                // The application can continue the transaction here if needed, or take alternative action
            }
        });
        // end::get-catch[]
    }

    static void get() {
        // tag::get[]
        cluster.transactions().run((ctx) -> {
            TransactionGetResult doc = ctx.get(collection, "a-doc");
        });
        // end::get[]
    }

    static void getReadOwnWrites() {
        // tag::getReadOwnWrites[]
        cluster.transactions().run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());

            TransactionGetResult doc = ctx.get(collection, docId);
        });
        // end::getReadOwnWrites[]
    }

    static void replace() {
        // tag::replace[]
        cluster.transactions().run((ctx) -> {
            TransactionGetResult doc = ctx.get(collection, "doc-id");
            JsonObject content = doc.contentAs(JsonObject.class);
            content.put("transactions", "are awesome");
            ctx.replace(doc, content);
        });
        // end::replace[]
    }

    static void replaceReactive() {
        // tag::replaceReactive[]
        cluster.reactive().transactions().run((ctx) -> {
            return ctx.get(collection.reactive(), "doc-id").flatMap(doc -> {
                JsonObject content = doc.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                return ctx.replace(doc, content);
            });
        });
        // end::replaceReactive[]
    }

    static void remove() {
        // tag::remove[]
        cluster.transactions().run((ctx) -> {
            TransactionGetResult doc = ctx.get(collection, "doc-id");
            ctx.remove(doc);
        });
        // end::remove[]
    }

    static void removeReactive() {
        // tag::removeReactive[]
        cluster.reactive().transactions().run((ctx) -> {
            return ctx.get(collection.reactive(), "anotherDoc")
                    .flatMap(doc -> ctx.remove(doc));
        });
        // end::removeReactive[]
    }

    static int calculateLevelForExperience(int experience) {
        return experience / 10;
    }

    // tag::full[]
    public void playerHitsMonster(int damage, String playerId, String monsterId) {
        try {
            cluster.transactions().run((ctx) -> {
                var monsterDoc = ctx.get(collection, monsterId);
                var playerDoc = ctx.get(collection, playerId);

                int monsterHitpoints = monsterDoc.contentAs(JsonObject.class).getInt("hitpoints");
                int monsterNewHitpoints = monsterHitpoints - damage;

                if (monsterNewHitpoints <= 0) {
                    // Monster is killed. The remove is just for demoing, and a more realistic
                    // example would set a "dead" flag or similar.
                    ctx.remove(monsterDoc);

                    // The player earns experience for killing the monster
                    int experienceForKillingMonster = monsterDoc.contentAs(JsonObject.class)
                            .getInt("experienceWhenKilled");
                    int playerExperience = playerDoc.contentAs(JsonObject.class).getInt("experience");
                    int playerNewExperience = playerExperience + experienceForKillingMonster;
                    int playerNewLevel = calculateLevelForExperience(playerNewExperience);

                    var playerContent = playerDoc.contentAs(JsonObject.class);

                    playerContent.put("experience", playerNewExperience);
                    playerContent.put("level", playerNewLevel);

                    ctx.replace(playerDoc, playerContent);
                } else {
                    // Monster is damaged but still alive
                    var monsterContent = monsterDoc.contentAs(JsonObject.class);
                    monsterContent.put("hitpoints", monsterNewHitpoints);

                    ctx.replace(monsterDoc, monsterContent);
                }
            });
        } catch (TransactionCommitAmbiguousException e) {
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            throw logFailure(e);
        }
    }
    // end::full[]

    static void concurrency() {
        // tag::concurrency[]
        cluster.environment().eventBus().subscribe(event -> {
            if (event instanceof IllegalDocumentStateEvent) {
                // log this event for review
                log(((TransactionEvent) event).logs());
            }
        });
        // end::concurrency[]
    }

    private static void log(List<CoreTransactionLogMessage> logs) {
        // Application can write to their logs here
    }

    static void cleanupEvents() {
        // tag::cleanup-events[]
        cluster.environment().eventBus().subscribe(event -> {
            if (event instanceof TransactionCleanupAttemptEvent || event instanceof TransactionCleanupEndRunEvent) {
                log(((TransactionEvent) event).logs());
            }
        });
        // end::cleanup-events[]
    }

    static void rollbackCause() {
        final int costOfItem = 10;

        // tag::rollback-cause[]
        class BalanceInsufficient extends RuntimeException {
        }

        try {
            cluster.transactions().run((ctx) -> {
                var customer = ctx.get(collection, "customer-name");

                if (customer.contentAsObject().getInt("balance") < costOfItem) {
                    throw new BalanceInsufficient();
                }
                // else continue transaction
            });
        } catch (TransactionCommitAmbiguousException e) {
            // This exception can only be thrown at the commit point, after the
            // BalanceInsufficient logic has been passed, so there is no need to
            // check getCause here.
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            if (e.getCause() instanceof BalanceInsufficient) {
                // Re-raise the error
                throw (RuntimeException) e.getCause();
            } else {
                throw logFailure(e);
            }
        }
        // end::rollback-cause[]
    }

    static void configExpiration() {
        // tag::config-expiration[]
        var cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig.timeout(Duration.ofSeconds(120)))));
        // end::config-expiration[]
    }

    static void configExpirationPer() {
        // tag::config-expiration-per[]
        cluster.transactions().run((ctx) -> {
            // Your transaction logic
        }, transactionOptions().timeout(Duration.ofSeconds(60)));
        // end::config-expiration-per[]
    }

    static void configCleanup() {
        var keyspace = TransactionKeyspace.create("bucketName", "scopeName", "collectionName");

        // tag::config-cleanup[]
        var cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig.cleanupConfig(TransactionsCleanupConfig
                        .cleanupClientAttempts(true)
                        .cleanupLostAttempts(true)
                        .cleanupWindow(Duration.ofSeconds(120))
                        .addCollections(List.of(keyspace))))));
        // end::config-cleanup[]
    }

    static void concurrentOps() {
        // tag::concurrentOps[]
        List<String> docIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");
        int concurrency = 100; // This many operations will be in-flight at once

        var result = cluster.reactive().transactions().run((ctx) -> {
            return Flux.fromIterable(docIds)
                    .parallel(concurrency)
                    .runOn(Schedulers.boundedElastic())
                    .concatMap(docId -> ctx.get(collection.reactive(), docId)
                            .flatMap(doc -> {
                                var content = doc.contentAsObject();
                                content.put("value", "updated");
                                return ctx.replace(doc, content);
                            }))
                    .sequential()
                    .then();
        }).block();
        // end::concurrentOps[]
    }

    static void completeErrorHandling() {
        // tag::full-error-handling[]
        try {
            TransactionResult result = cluster.transactions().run((ctx) -> {
                // ... transactional code here ...
            });

            // The transaction definitely reached the commit point.
            // Unstaging the individual documents may or may not have completed

            if (!result.unstagingComplete()) {
                // In rare cases, the application may require the commit to have
                // completed (recall that the asynchronous cleanup process is
                // still working to complete the commit).
                // The next step is application-dependent.
            }
        } catch (TransactionCommitAmbiguousException e) {
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            throw logFailure(e);
        }
        // end::full-error-handling[]
    }

    static void queryExamples() {
        // tag::queryExamplesSelect[]
        cluster.transactions().run((ctx) -> {
            var st = "SELECT * FROM `travel-sample`.inventory.hotel WHERE country = $1";
            var qr = ctx.query(st, TransactionQueryOptions.queryOptions()
                    .parameters(JsonArray.from("United Kingdom")));
            var rows = qr.rowsAs(JsonObject.class);
        });
        // end::queryExamplesSelect[]

        // tag::queryExamplesSelectScope[]
        Bucket travelSample = cluster.bucket("travel-sample");
        Scope inventory = travelSample.scope("inventory");

        cluster.transactions().run((ctx) -> {
            var qr = ctx.query(inventory, "SELECT * FROM hotel WHERE country = $1",
                    TransactionQueryOptions.queryOptions()
                            .parameters(JsonArray.from("United States")));
            var rows = qr.rowsAs(JsonObject.class);
        });
        // end::queryExamplesSelectScope[]

        // tag::queryExamplesUpdate[]
        String hotelChain = "http://marriot%";
        String country = "United States";

        cluster.transactions().run((ctx) -> {
            var qr = ctx.query(inventory, "UPDATE hotel SET price = $1 WHERE url LIKE $2 AND country = $3",
                    TransactionQueryOptions.queryOptions()
                            .parameters(JsonArray.from(99.99, hotelChain, country)));
            assert(qr.metaData().metrics().get().mutationCount() == 1);
        });
        // end::queryExamplesUpdate[]

        // tag::queryExamplesComplex[]
        cluster.transactions().run((ctx) -> {
            // Find all hotels of the chain
            var qr = ctx.query(inventory, "SELECT reviews FROM hotel WHERE url LIKE $1 AND country = $2",
                    TransactionQueryOptions.queryOptions()
                            .parameters(JsonArray.from(hotelChain, country)));

            // This function (not provided here) will use a trained machine learning model to provide a
            // suitable price based on recent customer reviews.
            double updatedPrice = priceFromRecentReviews(qr);

            // Set the price of all hotels in the chain
            ctx.query(inventory, "UPDATE hotel SET price = $1 WHERE url LIKE $2 AND country = $3",
                    TransactionQueryOptions.queryOptions()
                            .parameters(JsonArray.from(updatedPrice, hotelChain, country)));
        });
        // end::queryExamplesComplex[]
    }

    static double priceFromRecentReviews(TransactionQueryResult qr) {
        return 1.0;
    }

    static void queryInsert() {
        // tag::queryInsert[]
        cluster.transactions().run((ctx) -> {
            ctx.query("INSERT INTO `default` VALUES ('doc', {'hello':'world'})");  // <1>

            // Performing a 'Read Your Own Write'
            var st = "SELECT `default`.* FROM `default` WHERE META().id = 'doc'"; // <2>
            var qr = ctx.query(st);
            assert(qr.metaData().metrics().get().resultCount() == 1);
        });
        // end::queryInsert[]
    }

    static void querySingle() {
        String bulkLoadStatement = null; // a bulk-loading N1QL statement.  Left out of example per DOC-9630.

        // tag::querySingle[]
        try {
            var result = cluster.query(bulkLoadStatement, QueryOptions.queryOptions().asTransaction());
        } catch (TransactionCommitAmbiguousException e) {
            throw logCommitAmbiguousError(e);
        } catch (TransactionFailedException e) {
            throw logFailure(e);
        } catch (CouchbaseException e) {
            // Any standard query errors can be raised here too, such as ParsingFailureException.  In these cases the
            // transaction definitely did not reach commit point.
            logger.warning("Transaction did not reach commit point");
            throw e;
        }
        // end::querySingle[]
    }

    // tag::logger[]
    public static RuntimeException logCommitAmbiguousError(TransactionCommitAmbiguousException err) {
        // This example will intentionally not compile: the application needs to use
        // its own logging system to replace `logger`.
        logger.warning("Transaction possibly reached the commit point");

        for (TransactionLogEvent msg : err.logs()) {
            logger.warning(msg.toString());
        }

        return err;
    }

    public static RuntimeException logFailure(TransactionFailedException err) {
        logger.warning("Transaction did not reach commit point");

        for (TransactionLogEvent msg : err.logs()) {
            logger.warning(msg.toString());
        }

        return err;
    }
    // end::logger[]

    static void querySingleScoped() {
        String bulkLoadStatement = null /* your statement here */;

        // tag::querySingleScoped[]
        Bucket travelSample = cluster.bucket("travel-sample");
        Scope inventory = travelSample.scope("inventory");

        inventory.query(bulkLoadStatement, QueryOptions.queryOptions().asTransaction());
        // end::querySingleScoped[]
    }

    static void querySingleConfigured() {
        String bulkLoadStatement = null; /* your statement here */

        // tag::querySingleConfigured[]
        cluster.query(bulkLoadStatement, QueryOptions.queryOptions()
                // Single query transactions will often want to increase the default timeout
                .timeout(Duration.ofSeconds(360))
                .asTransaction(singleQueryTransactionOptions()
                        .durabilityLevel(DurabilityLevel.PERSIST_TO_MAJORITY)));
        // end::querySingleConfigured[]
    }

    static void queryRyow() {
        // tag::queryRyow[]
        cluster.transactions().run((ctx) -> {
            ctx.insert(collection, "doc", JsonObject.create().put("hello", "world")); // <1>

            // Performing a 'Read Your Own Write'
            var st = "SELECT `default`.* FROM `default` WHERE META().id = 'doc'"; // <2>
            var qr = ctx.query(st);
            assert(qr.metaData().metrics().get().resultCount() == 1);
        });
        // end::queryRyow[]
    }

    static void queryOptions() {
        // tag::queryOptions[]
        cluster.transactions().run((ctx) -> {
            ctx.query("INSERT INTO `default` VALUES ('doc', {'hello':'world'})",
                    TransactionQueryOptions.queryOptions().profile(QueryProfile.TIMINGS));
        });
        // end::queryOptions[]
    }

    static void customMetadata() {
        // tag::custom-metadata[]
        var keyspace = TransactionKeyspace.create("bucketName", "scopeName", "collectionName");

        var cluster = Cluster.connect("localhost", ClusterOptions.clusterOptions("username", "password")
                .environment(env -> env.transactionsConfig(TransactionsConfig.metadataCollection(keyspace))));
        // end::custom-metadata[]
    }

    static void customMetadataPer() {
        // tag::custom-metadata-per[]
        cluster.transactions().run((ctx) -> {
            // Your transaction logic
        }, transactionOptions().metadataCollection(collection));
        // end::custom-metadata-per[]
    }

    static void tracing() {
        // #tag::tracing[]
        var span = cluster.environment().requestTracer().requestSpan("your-span-name", null);

        cluster.transactions().run((ctx) -> {
            // your transaction
        }, transactionOptions().parentSpan(span));
        // #end::tracing[]
    }

    static void tracingWrapped() {
        // #tag::tracing-wrapped[]
        var span = Span.current(); // this is a span created by your code earlier
        var wrapped = OpenTelemetryRequestSpan.wrap(span);

        cluster.transactions().run((ctx) -> {
            // your transaction
        }, transactionOptions().parentSpan(wrapped));
        // #end::tracing-wrapped[]
    }

}
