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

import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;

import java.time.Duration;

import com.couchbase.client.core.error.CasMismatchException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;

public class Cas {

  public static void main(String... args) {

    Cluster cluster = Cluster.connect("localhost", "Administrator", "password");

    Bucket bucket = cluster.bucket("travel-sample");
    Collection collection = bucket.defaultCollection();

    {
      // tag::handlingerrors[]
      int maxRetries = 10;

      for (int i = 0; i < maxRetries; i++) {
        // Get the current document contents
        GetResult getResult = collection.get("airline_10");

        // Increment id on the airline
        JsonObject content = getResult.contentAsObject();
        content.put("id", content.getLong("id") + 1);

        try {
          // Attempt to replace the document with cas
          collection.replace("airline_10", content, replaceOptions().cas(getResult.cas()));
          break;
        } catch (CasMismatchException ex) {
          // continue the loop on cas mismatch to try again
          // note that any other exception will be raised and break the loop as well
        }
      }
      // end::handlingerrors[]
    }

    {
      // tag::locking[]
      GetResult getAndLockResult = collection.getAndLock("airline_1191", Duration.ofSeconds(2));

      long lockedCas = getAndLockResult.cas();

      /*
       * an example of simply unlocking the document: collection.unlock("key",
       * lockedCas);
       */

      collection.replace("airline_1191", "new value", replaceOptions().cas(lockedCas));
      // end::locking[]
    }
  }

}
