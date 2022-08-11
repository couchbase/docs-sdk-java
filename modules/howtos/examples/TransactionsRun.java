import com.couchbase.client.core.cnc.events.transaction.TransactionLogEvent;
import com.couchbase.client.core.error.DocumentExistsException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.transactions.TransactionGetResult;
import com.couchbase.client.java.transactions.TransactionResult;
import com.couchbase.client.java.transactions.config.TransactionsCleanupConfig;
import com.couchbase.client.java.transactions.config.TransactionsConfig;
import com.couchbase.client.java.transactions.config.TransactionsQueryConfig;
import com.couchbase.client.java.transactions.error.TransactionCommitAmbiguousException;
import com.couchbase.client.java.transactions.error.TransactionFailedException;

import java.time.Duration;

public class TransactionsRun {

	public static void main(String... args) {
		String connectionString = "couchbases://cb.zozhfipwvsza8zhl.cloud.couchbase.com";
//		Cluster cluster = Cluster.connect(connectionString, "username", "Password1!");
		Cluster cluster = Cluster.connect(connectionString, ClusterOptions.clusterOptions("username", "Password1!")
				.environment(env -> env.transactionsConfig(TransactionsConfig
						.durabilityLevel(DurabilityLevel.NONE)
						.cleanupConfig(TransactionsCleanupConfig
								.cleanupWindow(Duration.ofSeconds(30)))
						.queryConfig(TransactionsQueryConfig
								.scanConsistency(QueryScanConsistency.NOT_BOUNDED)))));

		Bucket bucket = cluster.bucket("travel-sample");
		Scope scope = bucket.scope("inventory");
		Collection collection = scope.collection("airport");

		{
			try {
				TransactionResult result = cluster.transactions().run((ctx) -> {
					// Inserting a doc:
					TransactionGetResult getResult = ctx.insert(collection, "doc-a", JsonObject.create());

					// Getting and replacing a doc:
					TransactionGetResult docA = ctx.get(collection, "doc-b");
					JsonObject content = docA.contentAs(JsonObject.class);
					content.put("transactions", "are awesome");
					ctx.replace(docA, content);

					// Getting and removing a doc:
					docA = ctx.get(collection, "doc-a");
					ctx.remove(docA);
				});

				result.logs().forEach(message -> System.out.println(message.toString()));
			} catch (TransactionCommitAmbiguousException e) {
				for (TransactionLogEvent evt : e.logs()) {
					System.out.println("ERROR: " + evt.toString());
				}
			} catch (TransactionFailedException e) {
				for (TransactionLogEvent evt : e.logs()) {
					System.out.println("ERROR: " + evt.toString());
				}
			}
		}
	}
}
