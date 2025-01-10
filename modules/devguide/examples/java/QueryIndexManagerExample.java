/*
 * Copyright (c) 2022 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.time.Duration;
import java.util.Arrays;
import com.couchbase.client.core.error.IndexExistsException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.query.BuildQueryIndexOptions;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.CreateQueryIndexOptions;
import com.couchbase.client.java.manager.query.DropPrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.DropQueryIndexOptions;
import com.couchbase.client.java.manager.query.QueryIndexManager;
import com.couchbase.client.java.manager.query.WatchQueryIndexesOptions;

public class QueryIndexManagerExample {

	public static void main(String... args) {
		// tag::creating-index-mgr[]
		Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
		QueryIndexManager queryIndexMgr = cluster.queryIndexes();
		// end::creating-index-mgr[]

		{
			System.out.println("[primary]");

			// tag::primary[]
			CreatePrimaryQueryIndexOptions opts = CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions()
					.scopeName("tenant_agent_01")
					.collectionName("users")
					// Set this if you wish to use a custom name
					// .indexName("custom_name") 
					.ignoreIfExists(true);

			queryIndexMgr.createPrimaryIndex("travel-sample", opts);
			// end::primary[]
		}

		{
			System.out.println("[secondary]");

			// tag::secondary[]
			try {
				CreateQueryIndexOptions opts = CreateQueryIndexOptions.createQueryIndexOptions()
						.scopeName("tenant_agent_01")
						.collectionName("users");

				queryIndexMgr.createIndex("travel-sample", "tenant_agent_01_users_email",
						Arrays.asList("preferred_email"), opts);
			} catch (IndexExistsException e) {
				System.out.println("Index already exists");
			}
			// end::secondary[]
		}

		{
			System.out.println("[defer-indexes]");

			// tag::defer-indexes[]
			try {
				// Create a deferred index
				CreateQueryIndexOptions createOpts = CreateQueryIndexOptions.createQueryIndexOptions()
						.scopeName("tenant_agent_01")
						.collectionName("users")
						.deferred(true);

				queryIndexMgr.createIndex("travel-sample", "tenant_agent_01_users_phone",
						Arrays.asList("preferred_phone"), createOpts);

				// Build any deferred indexes within `travel-sample`.tenant_agent_01.users
				BuildQueryIndexOptions deferredOpts = BuildQueryIndexOptions.buildDeferredQueryIndexesOptions()
						.scopeName("tenant_agent_01")
						.collectionName("users");

				queryIndexMgr.buildDeferredIndexes("travel-sample", deferredOpts);

				// Wait for indexes to come online
				WatchQueryIndexesOptions watchOpts = WatchQueryIndexesOptions.watchQueryIndexesOptions()
						.scopeName("tenant_agent_01")
						.collectionName("users");

				queryIndexMgr.watchIndexes("travel-sample", Arrays.asList("tenant_agent_01_users_phone"), 
						Duration.ofSeconds(60), watchOpts);

			} catch (IndexExistsException e) {
				System.out.println("Index already exists");
			}
			// end::defer-indexes[]
		}

		{
			System.out.println("[drop-primary-or-secondary-index]");

			// tag::drop-primary-or-secondary-index[]
			DropPrimaryQueryIndexOptions primaryIndexOpts = DropPrimaryQueryIndexOptions.dropPrimaryQueryIndexOptions()
					.scopeName("tenant_agent_01")
					.collectionName("users");	

			queryIndexMgr.dropPrimaryIndex("travel-sample", primaryIndexOpts);

			// Drop a secondary index
			DropQueryIndexOptions indexOpts = DropQueryIndexOptions.dropQueryIndexOptions()
					.scopeName("tenant_agent_01")
					.collectionName("users");	

			queryIndexMgr.dropIndex("travel-sample", "tenant_agent_01_users_email", indexOpts);
			// end::drop-primary-or-secondary-index[]
		}
	}
}
