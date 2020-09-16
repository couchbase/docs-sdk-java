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
import com.couchbase.client.java.Collection;

// #tag::imports[]
// Imports required by this sample
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.transactions.TransactionResult;
import com.couchbase.transactions.Transactions;
import com.couchbase.transactions.error.TransactionCommitAmbiguous;
import com.couchbase.transactions.error.TransactionFailed;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
// #end::imports[]

class TransactionsDemo {
    void demo_1_0_1_changes() {
        Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
        Bucket bucket = cluster.bucket("default");
        Collection collection = bucket.defaultCollection();
        Transactions transactions = Transactions.create(cluster);

        // #tag::demo_1_0_1[]
        try {
            TransactionResult result = transactions.run((ctx) -> {
                // ... transactional code here ...
            });

            // The transaction definitely reached the commit point. Unstaging
            // the individual documents may or may not have completed

            if (result.unstagingComplete()) {
                // Operations with non-transactional actors will want
                // unstagingComplete() to be true.
                cluster.query(" ... N1QL ... ",
                        queryOptions()
                                .consistentWith(result.mutationState()));

                String documentKey = "a document key involved in the transaction";
                GetResult getResult = collection.get(documentKey);
            }
            else {
                // This step is completely application-dependent.  It may
                // need to throw its own exception, if it is crucial that
                // result.unstagingComplete() is true at this point.
                // (Recall that the asynchronous cleanup process will
                // complete the unstaging later on).
            }
        }
        catch (TransactionCommitAmbiguous err) {
            // The transaction may or may not have reached commit point
            System.err.println("Transaction returned TransactionCommitAmbiguous and" +
                    " may have succeeded, logs:");

            // Of course, the application will want to use its own logging rather
            // than System.err
            err.result().log().logs().forEach(log -> System.err.println(log.toString()));
        }
        catch (TransactionFailed err) {
            // The transaction definitely did not reach commit point
            System.err.println("Transaction failed with TransactionFailed, logs:");
            err.result().log().logs().forEach(log -> System.err.println(log.toString()));
        }
        // #end::demo_1_0_1[]


        cluster.disconnect();
    }
}
