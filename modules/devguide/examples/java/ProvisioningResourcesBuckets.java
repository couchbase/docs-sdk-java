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

import com.couchbase.client.core.error.BucketExistsException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.bucket.BucketType;
import com.couchbase.client.java.manager.bucket.ConflictResolutionType;

public class ProvisioningResourcesBuckets {
	public static void main(String[] args) {
		// tag::creatingbucketmgr[]
		Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
		BucketManager bucketMgr = cluster.buckets();
		// end::creatingbucketmgr[]

		{
			System.out.println("[createBucket]");

			// tag::createBucket[]
			try {
				BucketSettings bucketSettings = BucketSettings.create("hello")
						.flushEnabled(false)
						.replicaIndexes(true)
						.ramQuotaMB(150)
						.numReplicas(1)
						.bucketType(BucketType.COUCHBASE)
						.conflictResolutionType(ConflictResolutionType.SEQUENCE_NUMBER);

				bucketMgr.createBucket(bucketSettings);
			} catch (BucketExistsException e) {
				System.out.println("Bucket already exists");
			}
			// end::createBucket[]

			// Wait until bucket is created before trying to flush it
			Bucket bucket = cluster.bucket("hello");
			bucket.waitUntilReady(Duration.ofSeconds(10));
		}

		{
			System.out.println("[updateBucket]");

			// tag::updateBucket[]
			BucketSettings settings = bucketMgr.getBucket("hello");
			settings.flushEnabled(true);

			bucketMgr.updateBucket(settings);
			// end::updateBucket[]
		}
		{
			System.out.println("[flushBucket]");


			// tag::flushBucket[]
			bucketMgr.flushBucket("hello");
			// end::flushBucket[]
		}
		{
			System.out.println("[removeBucket]");

			// tag::removeBucket[]
			bucketMgr.dropBucket("hello");
			// end::removeBucket[]
		}
	}
}
