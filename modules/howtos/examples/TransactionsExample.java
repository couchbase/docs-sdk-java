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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.couchbase.client.core.cnc.Event;
import com.couchbase.client.core.cnc.RequestSpan;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryProfile;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.tracing.opentelemetry.OpenTelemetryRequestSpan;
import com.couchbase.transactions.TransactionDurabilityLevel;
import com.couchbase.transactions.TransactionGetResult;
import com.couchbase.transactions.TransactionQueryOptions;
import com.couchbase.transactions.TransactionResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.PerTransactionConfigBuilder;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.couchbase.transactions.deferred.TransactionSerializedContext;
import com.couchbase.transactions.error.TransactionCommitAmbiguous;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.transactions.log.IllegalDocumentState;
import com.couchbase.transactions.log.LogDefer;
import com.couchbase.transactions.log.TransactionCleanupAttempt;
import com.couchbase.transactions.log.TransactionCleanupEndRunEvent;

import io.opentelemetry.api.trace.Span;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// end::imports[]

public class TransactionsExample {
    static Cluster cluster;
    static Collection collection;
    static Transactions transactions;

    public static void main(String... args) {
        // tag::init[]
        // Initialize the Couchbase cluster
        Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
        Bucket bucket = cluster.bucket("travel-sample");
        Collection collection = bucket.defaultCollection();

        // Create the single Transactions object
        Transactions transactions = Transactions.create(cluster);
        // end::init[]

        TransactionsExample.cluster = cluster;
        TransactionsExample.collection = collection;
        TransactionsExample.transactions = transactions;
    }

    static void config() {
        // tag::config[]
        Transactions transactions = Transactions.create(cluster,
                TransactionConfigBuilder.create().durabilityLevel(TransactionDurabilityLevel.PERSIST_TO_MAJORITY)
                        // tag::config_warn[]
                        .logOnFailure(true, Event.Severity.WARN)
                        // end::config_warn[]
                        .build());
        // end::config[]
    }

    static void create() {
        // tag::create[]
        try {
            transactions.run((ctx) -> {
                // 'ctx' is an AttemptContext, which permits getting, inserting,
                // removing and replacing documents, along with committing and
                // rolling back the transaction.

                // ... Your transaction logic here ...

                // This call is optional - if you leave it off, the transaction
                // will be committed anyway.
                ctx.commit();
            });
            // tag::logging[]
        } catch (TransactionCommitAmbiguous e) {
            // The application will of course want to use its own logging rather
            // than System.err
            System.err.println("Transaction possibly committed");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        } catch (TransactionFailed e) {
            System.err.println("Transaction did not reach commit point");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        }
        // end::logging[]
        // end::create[]
    }

    static void createReactive() {
        // tag::createReactive[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            // 'ctx' is an AttemptContextReactive, providing asynchronous versions of the
            // AttemptContext methods

            return

            // Your transaction logic here: as an example, get and remove a doc
            ctx.get(collection.reactive(), "document-id").flatMap(doc -> ctx.remove(doc))

                    // The commit call is optional - if you leave it off,
                    // the transaction will be committed anyway.
                    .then(ctx.commit());
            // tag::async_logging[]
        }).doOnError(err -> {
            if (err instanceof TransactionCommitAmbiguous) {
                System.err.println("Transaction possibly committed: ");
            } else {
                System.err.println("Transaction failed: ");
            }

            for (LogDefer e : ((TransactionFailed) err).result().log().logs()) {
                // System.err is used for example, log failures to your own logging system
                System.err.println(err.toString());
            }
        });
        // end::async_logging[]

        // Normally you will chain this result further and ultimately subscribe. For
        // simplicity, here we just block
        // on the result.
        TransactionResult finalResult = result.block();
        // end::createReactive[]
    }

    static void examples() {
        // tag::examples[]
        try {
            TransactionResult result = transactions.run((ctx) -> {
                // Inserting a doc:
                ctx.insert(collection, "doc-a", JsonObject.create());

                // Getting documents:
                // Use ctx.getOptional if the document may or may not exist
                Optional<TransactionGetResult> docOpt = ctx.getOptional(collection, "doc-a");

                // Use ctx.get if the document should exist, and the transaction
                // will fail if it does not
                TransactionGetResult docA = ctx.get(collection, "doc-a");

                // Replacing a doc:
                TransactionGetResult docB = ctx.get(collection, "doc-b");
                JsonObject content = docB.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                ctx.replace(docB, content);

                // Removing a doc:
                TransactionGetResult docC = ctx.get(collection, "doc-c");
                ctx.remove(docC);

                ctx.commit();
            });
        } catch (TransactionCommitAmbiguous e) {
            System.err.println("Transaction possibly committed");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        } catch (TransactionFailed e) {
            System.err.println("Transaction did not reach commit point");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        }
        // end::examples[]
    }

    static void examplesReactive() {
        // tag::examplesReactive[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            return
            // Inserting a doc:
            ctx.insert(collection.reactive(), "doc-a", JsonObject.create())

                    // Getting and replacing a doc:
                    .then(ctx.get(collection.reactive(), "doc-b")).flatMap(docB -> {
                        JsonObject content = docB.contentAs(JsonObject.class);
                        content.put("transactions", "are awesome");
                        return ctx.replace(docB, content);
                    })

                    // Getting and removing a doc:
                    .then(ctx.get(collection.reactive(), "doc-c")).flatMap(doc -> ctx.remove(doc))

                    // Committing:
                    .then(ctx.commit());

        }).doOnError(err -> {
            if (err instanceof TransactionCommitAmbiguous) {
                System.err.println("Transaction possibly committed: ");
            } else {
                System.err.println("Transaction failed: ");
            }

            for (LogDefer e : ((TransactionFailed) err).result().log().logs()) {
                // System.err is used for example, log failures to your own logging system
                System.err.println(err.toString());
            }
        });

        // Normally you will chain this result further and ultimately subscribe.
        // For simplicity, here we just block on the result.
        result.block();
        // end::examplesReactive[]

    }

    static void insert() {
        // tag::insert[]
        transactions.reactive().run((ctx) -> {
            return ctx.insert(collection.reactive(), "docId", JsonObject.create()).then();
        }).block();
        // end::insert[]
    }

    static void insertReactive() {
        // tag::insertReactive[]
        transactions.run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());

        });
        // end::insertReactive[]
    }

    static void get() {
        // tag::get[]
        transactions.run((ctx) -> {
            String docId = "a-doc";

            Optional<TransactionGetResult> docOpt = ctx.getOptional(collection, docId);
            TransactionGetResult doc = ctx.get(collection, docId);
        });
        // end::get[]
    }

    static void getReadOwnWrites() {
        // tag::getReadOwnWrites[]
        transactions.run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());

            Optional<TransactionGetResult> doc = ctx.getOptional(collection, docId);

            assert (doc.isPresent());
        });
        // end::getReadOwnWrites[]
    }

    static void replace() {
        // tag::replace[]
        transactions.run((ctx) -> {
            TransactionGetResult anotherDoc = ctx.get(collection, "anotherDoc");
            JsonObject content = anotherDoc.contentAs(JsonObject.class);
            content.put("transactions", "are awesome");
            ctx.replace(anotherDoc, content);
        });
        // end::replace[]
    }

    static void replaceReactive() {
        // tag::replaceReactive[]
        transactions.reactive().run((ctx) -> {
            return ctx.get(collection.reactive(), "anotherDoc").flatMap(doc -> {
                JsonObject content = doc.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                return ctx.replace(doc, content);
            }).then(ctx.commit());
        });
        // end::replaceReactive[]
    }

    static void remove() {
        // tag::remove[]
        transactions.run((ctx) -> {
            TransactionGetResult anotherDoc = ctx.get(collection, "anotherDoc");
            ctx.remove(anotherDoc);
        });
        // end::remove[]
    }

    static void removeReactive() {
        // tag::removeReactive[]
        transactions.reactive().run((ctx) -> {
            return ctx.get(collection.reactive(), "anotherDoc").flatMap(doc -> ctx.remove(doc));
        });
        // end::removeReactive[]
    }

    static void commit() {
        // tag::commit[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            return ctx.get(collection.reactive(), "anotherDoc").flatMap(doc -> {
                JsonObject content = doc.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                return ctx.replace(doc, content);
            }).then();
        });
        // end::commit[]
    }

    static Transactions getTransactions() {
        return transactions;
    }

    static int calculateLevelForExperience(int experience) {
        return experience / 10;
    }

    // tag::full[]
    public void playerHitsMonster(int damage, String playerId, String monsterId) {
        Transactions transactions = getTransactions();

        try {
            transactions.run((ctx) -> {
                TransactionGetResult monsterDoc = ctx.get(collection, monsterId);
                TransactionGetResult playerDoc = ctx.get(collection, playerId);

                int monsterHitpoints = monsterDoc.contentAs(JsonObject.class).getInt("hitpoints");
                int monsterNewHitpoints = monsterHitpoints - damage;

                if (monsterNewHitpoints <= 0) {
                    // Monster is killed. The remove is just for demoing, and a more realistic
                    // example would set a
                    // "dead" flag or similar.
                    ctx.remove(monsterDoc);

                    // The player earns experience for killing the monster
                    int experienceForKillingMonster = monsterDoc.contentAs(JsonObject.class)
                            .getInt("experienceWhenKilled");
                    int playerExperience = playerDoc.contentAs(JsonObject.class).getInt("experience");
                    int playerNewExperience = playerExperience + experienceForKillingMonster;
                    int playerNewLevel = calculateLevelForExperience(playerNewExperience);

                    JsonObject playerContent = playerDoc.contentAs(JsonObject.class);

                    playerContent.put("experience", playerNewExperience);
                    playerContent.put("level", playerNewLevel);

                    ctx.replace(playerDoc, playerContent);
                } else {
                    // Monster is damaged but still alive
                    JsonObject monsterContent = monsterDoc.contentAs(JsonObject.class);
                    monsterContent.put("hitpoints", monsterNewHitpoints);

                    ctx.replace(monsterDoc, monsterContent);
                }
            });
        } catch (TransactionFailed e) {
            // The operation failed. Both the monster and the player will be untouched.

            // Situations that can cause this would include either the monster
            // or player not existing (as get is used), or a persistent
            // failure to be able to commit the transaction, for example on
            // prolonged node failure.
        }
    }
    // end::full[]

    static void concurrency() {
        // tag::concurrency[]
        cluster.environment().eventBus().subscribe(event -> {
            if (event instanceof IllegalDocumentState) {
                // log this event for review
            }
        });
        // end::concurrency[]
    }

    static void cleanupEvents() {
        // tag::cleanup-events[]
        cluster.environment().eventBus().subscribe(event -> {
            if (event instanceof TransactionCleanupAttempt || event instanceof TransactionCleanupEndRunEvent) {
                // log this event
            }
        });
        // end::cleanup-events[]
    }

    static void rollback() {
        final int costOfItem = 10;

        // tag::rollback[]
        transactions.run((ctx) -> {
            TransactionGetResult customer = ctx.get(collection, "customer-name");

            if (customer.contentAsObject().getInt("balance") < costOfItem) {
                ctx.rollback();
            }
            // else continue transaction
        });
        // end::rollback[]
    }

    static void rollbackCause() {
        final int costOfItem = 10;

        // tag::rollback-cause[]
        class BalanceInsufficient extends RuntimeException {
        }

        try {
            transactions.run((ctx) -> {
                TransactionGetResult customer = ctx.get(collection, "customer-name");

                if (customer.contentAsObject().getInt("balance") < costOfItem) {
                    throw new BalanceInsufficient();
                }
                // else continue transaction
            });
        } catch (TransactionCommitAmbiguous e) {
            // This exception can only be thrown at the commit point, after the
            // BalanceInsufficient logic has been passed, so there is no need to
            // check getCause here.
            System.err.println("Transaction possibly committed");
            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        } catch (TransactionFailed e) {
            if (e.getCause() instanceof BalanceInsufficient) {
                // Re-raise the error
                throw (RuntimeException) e.getCause();
            } else {
                System.err.println("Transaction did not reach commit point");

                for (LogDefer err : e.result().log().logs()) {
                    System.err.println(err.toString());
                }
            }
        }
        // end::rollback-cause[]
    }

    static void deferredCommit1() {

        // tag::defer1[]
        try {
            TransactionResult result = transactions.run((ctx) -> {
                JsonObject initial = JsonObject.create().put("val", 1);
                ctx.insert(collection, "a-doc-id", initial);

                // Defer means don't do a commit right now. `serialized` in the result will be
                // present.
                ctx.defer();
            });

            // Available because ctx.defer() was called
            assert (result.serialized().isPresent());

            TransactionSerializedContext serialized = result.serialized().get();

            // This is going to store a serialized form of the transaction to pass around
            byte[] encoded = serialized.encodeAsBytes();

        } catch (TransactionFailed e) {
            // System.err is used for example, log failures to your own logging system
            System.err.println("Transaction did not reach commit point");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        }
        // end::defer1[]
    }

    static void deferredCommit2(byte[] encoded) {
        // tag::defer2[]
        TransactionSerializedContext serialized = TransactionSerializedContext.createFrom(encoded);

        try {
            TransactionResult result = transactions.commit(serialized);

        } catch (TransactionFailed e) {
            // System.err is used for example, log failures to your own logging system
            System.err.println("Transaction did not reach commit point");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        }
        // end::defer2[]
    }

    static void deferredRollback(byte[] encoded) {
        // tag::defer3[]
        TransactionSerializedContext serialized = TransactionSerializedContext.createFrom(encoded);

        try {
            TransactionResult result = transactions.rollback(serialized);

        } catch (TransactionFailed e) {
            // System.err is used for example, log failures to your own logging system
            System.err.println("Transaction did not reach commit point");

            for (LogDefer err : e.result().log().logs()) {
                System.err.println(err.toString());
            }
        }
        // end::defer3[]
    }

    static void configExpiration(byte[] encoded) {
        // tag::config-expiration[]
        Transactions transactions = Transactions.create(cluster,
                TransactionConfigBuilder.create().expirationTime(Duration.ofSeconds(120)).build());
        // end::config-expiration[]
    }

    static void configCleanup(byte[] encoded) {
        // tag::config-cleanup[]
        Transactions transactions = Transactions.create(cluster,
                TransactionConfigBuilder.create().cleanupClientAttempts(false).cleanupLostAttempts(false)
                        .cleanupWindow(Duration.ofSeconds(120)).build());
        // end::config-cleanup[]
    }

    static void concurrentOps() {
        // tag::concurrentOps[]
        List<String> docIds = Arrays.asList("doc1", "doc2", "doc3", "doc4", "doc5");

        ReactiveCollection coll = collection.reactive();

        TransactionResult result = transactions.reactive((ctx) -> {

            // Tracks whether all operations were successful
            AtomicBoolean allOpsSucceeded = new AtomicBoolean(true);

            // The first mutation must be done in serial, as it also creates a metadata
            // entry
            return ctx.get(coll, docIds.get(0)).flatMap(doc -> {
                JsonObject content = doc.contentAsObject();
                content.put("value", "updated");
                return ctx.replace(doc, content);
            })

                    // Do all other docs in parallel
                    .thenMany(Flux.fromIterable(docIds.subList(1, docIds.size()))
                            .flatMap(docId -> ctx.get(coll, docId).flatMap(doc -> {
                                JsonObject content = doc.contentAsObject();
                                content.put("value", "updated");
                                return ctx.replace(doc, content);
                            }).onErrorResume(err -> {
                                allOpsSucceeded.set(false);
                                // App should replace this with logging
                                err.printStackTrace();

                                // Allow other ops to finish
                                return Mono.empty();
                            }),

                                    // Run these in parallel
                                    docIds.size())

            // The commit or rollback must also be done in serial
            ).then(Mono.defer(() -> {
                // Commit iff all ops succeeded
                if (allOpsSucceeded.get()) {
                    return ctx.commit();
                } else {
                    throw new RuntimeException("Retry the transaction");
                }
            }));
        }).block();
        // end::concurrentOps[]
    }

    static void completeErrorHandling() {
        // tag::full-error-handling[]
        try {
            TransactionResult result = transactions.run((ctx) -> {
                // ... transactional code here ...
            });

            // The transaction definitely reached the commit point. Unstaging
            // the individual documents may or may not have completed

            if (result.unstagingComplete()) {
                // Operations with non-transactional actors will want
                // unstagingComplete() to be true.
                // Note that result.mutationState() is only available if the
                // transaction exclusively involves KV operations (no N1QL queries).
                cluster.query(" ... N1QL ... ", QueryOptions.queryOptions().consistentWith(result.mutationState()));

                String documentKey = "a document key involved in the transaction";
                GetResult getResult = collection.get(documentKey);
            } else {
                // This step is completely application-dependent. It may
                // need to throw its own exception, if it is crucial that
                // result.unstagingComplete() is true at this point.
                // (Recall that the asynchronous cleanup process will
                // complete the unstaging later on).
            }
        } catch (TransactionCommitAmbiguous err) {
            // The transaction may or may not have reached commit point
            System.err.println("Transaction returned TransactionCommitAmbiguous and" + " may have succeeded, logs:");

            // Of course, the application will want to use its own logging rather
            // than System.err
            err.result().log().logs().forEach(log -> System.err.println(log.toString()));
        } catch (TransactionFailed err) {
            // The transaction definitely did not reach commit point
            System.err.println("Transaction failed with TransactionFailed, logs:");
            err.result().log().logs().forEach(log -> System.err.println(log.toString()));
        }
        // end::full-error-handling[]
    }

    static void completeLogging() {
        // tag::full-logging[]
        final Logger LOGGER = Logger.getLogger("transactions");

        try {
            TransactionResult result = transactions.run((ctx) -> {
                // ... transactional code here ...
            });
        } catch (TransactionCommitAmbiguous err) {
            // The transaction may or may not have reached commit point
            LOGGER.info("Transaction returned TransactionCommitAmbiguous and" + " may have succeeded, logs:");
            err.result().log().logs().forEach(log -> LOGGER.info(log.toString()));
        } catch (TransactionFailed err) {
            // The transaction definitely did not reach commit point
            LOGGER.info("Transaction failed with TransactionFailed, logs:");
            err.result().log().logs().forEach(log -> LOGGER.info(log.toString()));
        }
        // end::full-logging[]
    }

    static void queryInsert() {
        // tag::queryInsert[]
        transactions.run((ctx) -> {
            ctx.query("INSERT INTO `default` VALUES ('doc', {'hello':'world'})");

            String st = "SELECT `default`.* FROM `default` WHERE META().id = 'doc'";
            QueryResult qr = ctx.query(st);
            qr.rowsAsObject().forEach(row -> {
                System.out.println(row);
            });
        });
        // end::queryInsert[]
    }

    static void queryRyow() {
        // tag::queryRyow[]
        transactions.run((ctx) -> {
            ctx.insert(collection, "doc", JsonObject.create().put("hello", "world"));

            String st = "SELECT `default`.* FROM `default` WHERE META().id = 'doc'";
            QueryResult qr = ctx.query(st);
            qr.rowsAsObject().forEach(row -> {
                System.out.println(row);
            });
        });
        // end::queryRyow[]
    }

    static void queryOptions() {
        // tag::queryOptions[]
        transactions.run((ctx) -> {
            ctx.query("INSERT INTO `default` VALUES ('doc', {'hello':'world'})",
                    TransactionQueryOptions.queryOptions().profile(QueryProfile.TIMINGS));
        });
        // end::queryOptions[]
    }

    static void customMetadata() {
        Collection metadataCollection = null;

        // tag::custom-metadata[]
        Transactions transactions = Transactions.create(cluster,
                TransactionConfigBuilder.create().metadataCollection(metadataCollection));
        // end::custom-metadata[]
    }

    static void tracing() {
        // #tag::tracing[]
        RequestSpan span = cluster.environment().requestTracer().requestSpan("your-span-name", null);

        transactions.run((ctx) -> {
            // your transaction
        }, PerTransactionConfigBuilder.create().parentSpan(span).build());
        // #end::tracing[]
    }

    static void tracingWrapped() {
        // #tag::tracing-wrapped[]
        Span dummySpan = Span.current(); // (this is a dummy span) created by your code earlier
        RequestSpan wrapped = OpenTelemetryRequestSpan.wrap(dummySpan);

        transactions.run((ctx) -> {
            // your transaction
        }, PerTransactionConfigBuilder.create().parentSpan(wrapped).build());
        // #end::tracing-wrapped[]
    }

}
