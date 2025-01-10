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

import java.util.HashMap;
import java.util.Map;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.view.DesignDocument;
import com.couchbase.client.java.manager.view.View;
import com.couchbase.client.java.manager.view.ViewIndexManager;
import com.couchbase.client.java.view.DesignDocumentNamespace;

public class ProvisioningResourcesViews {
	public static void main(String[] args) {
		// tag::viewmgr[]
		Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
		Bucket bucket = cluster.bucket("travel-sample");
		ViewIndexManager viewMgr = bucket.viewIndexes();
		// end::viewmgr[]

		{
			// tag::createView[]
			Map<String, View> views = new HashMap<>();
			views.put(
				"by_country",
				new View("function (doc, meta) { if (doc.type == 'landmark') { emit([doc.country, doc.city], null); } }")
			);
			views.put(
				"by_activity",
				new View(
					"function (doc, meta) { if (doc.type == 'landmark') { emit([doc.country, doc.city], null); } }",
					"_count")
			);

			DesignDocument designDocument = new DesignDocument("landmarks", views);
			viewMgr.upsertDesignDocument(designDocument, DesignDocumentNamespace.DEVELOPMENT);
			// end::createView[]
		}

		{
			// tag::getView[]
			DesignDocument designDocument = viewMgr.getDesignDocument("landmarks", DesignDocumentNamespace.DEVELOPMENT);
			System.out.print(designDocument);
			// end::getView[]
		}


		{
			// tag::publishView[]
			viewMgr.publishDesignDocument("landmarks");
			// end::publishView[]
		}

		{
			// tag::removeView[]
			viewMgr.dropDesignDocument("landmarks", DesignDocumentNamespace.PRODUCTION);
			// end::removeView[]
		}
	}
}
