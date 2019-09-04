// #tag::imports[]

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.transactions.TransactionJsonDocument;
import com.couchbase.transactions.TransactionResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.config.TransactionConfigBuilder;
import com.couchbase.transactions.error.TransactionFailed;
import com.couchbase.transactions.log.IllegalDocumentState;
import com.couchbase.transactions.log.LogDefer;
import reactor.core.publisher.Mono;

import java.util.Optional;
// #end::imports[]

public class TransactionsExample {
    static Cluster cluster;
    static Collection collection;
    static Transactions transactions;

    public static void main(String... args) {
        // #tag::init[]
        // Initialize the Couchbase cluster
        Cluster cluster = Cluster.connect("localhost", "transactor", "mypass");
        Bucket bucket = cluster.bucket("transact");
        Collection collection = bucket.defaultCollection();

        // Create the single Transactions object
        Transactions transactions = Transactions.create(cluster, TransactionConfigBuilder.create()
                // The configuration can be altered here, but in most cases the defaults are fine.
                .build());
        // #end::init[]

        TransactionsExample.cluster = cluster;
        TransactionsExample.collection = collection;
        TransactionsExample.transactions = transactions;
    }

    static void create() {
        // #tag::create[]
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
        } catch (TransactionFailed e) {
            for (LogDefer err : e.result().log().logs()) {
                // Optionally, log failures to your own logger
            }
        }
        // #end::create[]
    }

    static void createReactive() {
        // #tag::createReactive[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            // 'ctx' is an AttemptContextReactive, providing asynchronous versions of the AttemptContext methods

            return

                    // Your transaction logic here: as an example, get and remove a doc
                    ctx.getOrError(collection.reactive(), "document-id")
                            .flatMap(doc -> ctx.remove(doc))

                            // The commit call is optional - if you leave it off,
                            // the transaction will be committed anyway.
                            .then(ctx.commit());

        }).doOnError(err -> {
            if (err instanceof TransactionFailed) {
                for (LogDefer e : ((TransactionFailed) err).result().log().logs()) {
                    // Optionally, log failures to your own logger
                }
            }
        });

        // Normally you will chain this result further and ultimately subscribe.  For simplicity, here we just block
        // on the result.
        TransactionResult finalResult = result.block();
        // #end::createReactive[]
    }

    static void examples() {
        // #tag::examples[]
        try {
            transactions.run((ctx) -> {
                // Inserting a doc:
                String docId = "aDocument";
                ctx.insert(collection, docId, JsonObject.create());

                // Getting documents:
                // Use ctx.get if the document may or may not exist
                Optional<TransactionJsonDocument> docOpt = ctx.get(collection, docId);

                // Use ctx.getOrError if the document should exist, and the transaction will fail if not
                TransactionJsonDocument doc = ctx.getOrError(collection, docId);

                // Replacing a doc:
                TransactionJsonDocument anotherDoc = ctx.getOrError(collection, "anotherDoc");
                // TransactionJsonDocument is immutable, so get its content as a mutable JsonObject
                JsonObject content = anotherDoc.contentAs(JsonObject.class);
                content.put("transactions", "are awesome");
                ctx.replace(anotherDoc, content);

                // Removing a doc:
                TransactionJsonDocument yetAnotherDoc = ctx.getOrError(collection, "yetAnotherDoc");
                ctx.remove(yetAnotherDoc);

                ctx.commit();
            });
        } catch (TransactionFailed e) {
            for (LogDefer err : e.result().log().logs()) {
                // Optionally, log failures to your own logger
            }
        }
        // #end::examples[]
    }

    static void examplesReactive() {
        // #tag::examplesReactive[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            return
                    // Inserting a doc:
                    ctx.insert(collection.reactive(), "aDoc", JsonObject.create())

                            // Getting and replacing a doc:
                            .then(ctx.getOrError(collection.reactive(), "anotherDoc"))
                            .flatMap(doc -> {
                                JsonObject content = doc.contentAs(JsonObject.class);
                                content.put("transactions", "are awesome");
                                return ctx.replace(doc, content);
                            })

                            // Getting and removing a doc:
                            .then(ctx.getOrError(collection.reactive(), "yetAnotherDoc"))
                            .flatMap(doc -> ctx.remove(doc))

                            // Committing:
                            .then(ctx.commit());

        }).doOnError(err -> {
            if (err instanceof TransactionFailed) {
                for (LogDefer e : ((TransactionFailed) err).result().log().logs()) {
                    // Optionally, log failures to your own logger
                }
            }
        });

        // Normally you will chain this result further and ultimately subscribe.
        // For simplicity, here we just block on the result.
        result.block();
        // #end::examplesReactive[]

    }

    static void insert() {
        // #tag::insert[]
        transactions.reactive().run((ctx) -> {
            return ctx.insert(collection.reactive(), "docId", JsonObject.create()).then();
        }).block();
        // #end::insert[]
    }

    static void insertReactive() {
        // #tag::insertReactive[]
        transactions.run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());

        });
        // #end::insertReactive[]
    }

    static void get() {
        // #tag::get[]
        transactions.run((ctx) -> {
            String docId = "a-doc";

            Optional<TransactionJsonDocument> docOpt = ctx.get(collection, docId);
            TransactionJsonDocument doc = ctx.getOrError(collection, docId);
        });
        // #end::get[]
    }

    static void getReactive() {
        // #tag::getReactive[]
        transactions.run((ctx) -> {
            String docId = "docId";

            ctx.insert(collection, docId, JsonObject.create());

            Optional<TransactionJsonDocument> doc = ctx.get(collection, docId);

            assert (doc.isPresent());
        });
        // #end::getReactive[]
    }

    static void replace() {
        // #tag::replace[]
        transactions.run((ctx) -> {
            TransactionJsonDocument anotherDoc = ctx.getOrError(collection, "anotherDoc");
            JsonObject content = anotherDoc.contentAs(JsonObject.class);
            content.put("transactions", "are awesome");
            ctx.replace(anotherDoc, content);
        });
        // #end::replace[]
    }

    static void replaceReactive() {
        // #tag::replaceReactive[]
        transactions.reactive().run((ctx) -> {
            return ctx.getOrError(collection.reactive(), "anotherDoc")
                    .flatMap(doc -> {
                        JsonObject content = doc.contentAs(JsonObject.class);
                        content.put("transactions", "are awesome");
                        return ctx.replace(doc, content);
                    })
                    .then(ctx.commit());
        });
        // #end::replaceReactive[]
    }

    static void remove() {
        // #tag::remove[]
        transactions.run((ctx) -> {
            TransactionJsonDocument anotherDoc = ctx.getOrError(collection, "anotherDoc");
            ctx.remove(anotherDoc);
        });
        // #end::remove[]
    }

    static void removeReactive() {
        // #tag::removeReactive[]
        transactions.reactive().run((ctx) -> {
            return ctx.getOrError(collection.reactive(), "anotherDoc")
                    .flatMap(doc -> ctx.remove(doc));
        });
        // #end::removeReactive[]
    }

    static void commit() {
        // #tag::commit[]
        Mono<TransactionResult> result = transactions.reactive().run((ctx) -> {
            return ctx.getOrError(collection.reactive(), "anotherDoc")
                    .flatMap(doc -> {
                        JsonObject content = doc.contentAs(JsonObject.class);
                        content.put("transactions", "are awesome");
                        return ctx.replace(doc, content);
                    })
                    .then();
        });
        // #end::commit[]
    }

    static Transactions getTransactions() {
        return transactions;
    }

    static int calculateLevelForExperience(int experience) {
        return experience / 10;
    }

    // #tag::full[]
    public void playerHitsMonster(int damage, String playerId, String monsterId) {
        Transactions transactions = getTransactions();

        try {
            transactions.run((ctx) -> {
                TransactionJsonDocument monsterDoc = ctx.getOrError(collection, monsterId);
                TransactionJsonDocument playerDoc = ctx.getOrError(collection, playerId);

                int monsterHitpoints = monsterDoc.contentAs(JsonObject.class).getInt("hitpoints");
                int monsterNewHitpoints = monsterHitpoints - damage;

                if (monsterNewHitpoints <= 0) {
                    // Monster is killed.  The remove is just for demoing, and a more realistic example would set a
                    // "dead" flag or similar.
                    ctx.remove(monsterDoc);

                    // The player earns experience for killing the monster
                    int experienceForKillingMonster = monsterDoc.contentAs(JsonObject.class).getInt(
                            "experienceWhenKilled");
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
            // The operation failed.   Both the monster and the player will be untouched.

            // Situations that can cause this would include either the monster
            // or player not existing (as getOrError is used), or a persistent
            // failure to be able to commit the transaction, for example on
            // prolonged node failure.
        }
    }
    // #end::full[]

    static void concurrency() {
        // #tag::concurrency[]
        cluster.environment().eventBus().subscribe(event -> {
            if (event instanceof IllegalDocumentState) {
                // log this event for review
            }
        });
        // #end::concurrency[]
    }

    static void rollback() {
        // #tag::rollback[]
        transactions.run((ctx) -> {
            ctx.insert(collection, "docId", JsonObject.create());

            Optional<TransactionJsonDocument> docOpt = ctx.get(collection, "requiredDoc");
            if (docOpt.isPresent()) {
                ctx.remove(docOpt.get());
                ctx.commit();
            } else {
                ctx.rollback();
            }
        });
        // #end::rollback[]
    }

}



