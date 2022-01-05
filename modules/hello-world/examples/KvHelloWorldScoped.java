/*
 * Copyright (c) 2021 Couchbase, Inc.
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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.kv.LookupInResult;
import com.couchbase.client.java.kv.LookupInSpec;
import com.couchbase.client.java.kv.MutateInResult;
import com.couchbase.client.java.kv.MutateInSpec;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.ReplaceOptions;

public class KvHelloWorldScoped {

  public static void main(String[] args) {
    Cluster cluster = Cluster.connect("localhost", "Administrator", "password");
    Bucket bucket = cluster.bucket("travel-sample");
    Collection hotelCollection = bucket.scope("inventory").collection("hotel");

    {
      System.out.println("Example: [kv-insert]");
      // tag::kv-insert[]
      // Create the document object.
      JsonObject geo = JsonObject.create()
          .put("lat", 51.35785)
          .put("lon", 0.55818)
          .put("accuracy", "RANGE_INTERPOLATED");

      JsonArray reviews = JsonArray.create();
      reviews.add(JsonObject.create()
          .put("content", "This was our 2nd trip here and we enjoyed it more than last year.")
          .put("author", "Ozella Sipes")
          .put("date", DateTimeFormatter.ISO_INSTANT.format(Instant.now())));

      JsonObject document = JsonObject.create()
          .put("id", "hotel-123")
          .put("name", "Medway Youth Hostel")
          .put("address", "Capstone Road, ME7 3JE")
          .put("url", "http://www.yha.org.uk")
          .put("geo", geo)
          .put("country", "United Kingdom")
          .put("city", "Medway")
          .put("state", (String) null)
          .put("reviews", reviews)
          .put("vacancy", true)
          .put("description", "40 bed summer hostel about 3 miles from Gillingham.");

      // Insert the document in the hotel collection.
      MutationResult insertResult = hotelCollection.insert("hotel-123", document);

      // Print the result's CAS metadata to the console.
      System.out.println("CAS:" + insertResult.cas());
      // end::kv-insert[]
    }

    {
      System.out.println("Example: [kv-insert-with-opts]");
      // tag::kv-insert-with-opts[]
      JsonObject document = JsonObject.create()
          .put("id", 456)
          .put("title", "Ardèche")
          .put("name", "La Pradella")
          .put("address", "rue du village, 07290 Preaux, France")
          .put("phone", "+33 4 75 32 08 52")
          .put("url", "http://www.lapradella.fr")
          .put("country", "France")
          .put("city", "Preaux")
          .put("state", "Rhône-Alpes")
          .put("vacancy", false);

      // Insert the document with an expiry time option of 60 seconds.
      MutationResult insertResult = hotelCollection.insert(
        "hotel-456",
        document,
        InsertOptions.insertOptions().expiry(Duration.ofSeconds(2))
      );

      // Print the result's CAS metadata to the console.
      System.out.println("CAS:" + insertResult.cas());
      // end::kv-insert-with-opts[]
    }

    {
      System.out.println("Example: [kv-get]");
      // tag::kv-get[]
      GetResult getResult = hotelCollection.get("hotel-123");

      // Print the result's CAS metadata to the console.
      System.out.println("CAS:" + getResult.cas());
      // end::kv-get[]
    }

    {
      System.out.println("Example: [kv-get-with-opts]");
      // tag::kv-get-with-opts[]
      GetResult getResult = hotelCollection.get("hotel-123", 
          GetOptions.getOptions().withExpiry(true)
      );

      // Print the result's CAS metadata to the console.
      System.out.println("CAS:" + getResult.cas());
      System.out.println("Data:" + getResult.contentAsObject());
      System.out.println("Expiry:" + getResult.expiryTime());
      // end::kv-get-with-opts[]
    }

    {
      System.out.println("Example: [kv-get-subdoc]");
      // tag::kv-get-subdoc[]
      List<LookupInSpec> specs = Arrays.asList(LookupInSpec.get("geo"));

      LookupInResult lookupInResult = hotelCollection.lookupIn("hotel-123", specs);
      System.out.println("CAS:" + lookupInResult.cas());
      System.out.println("Geo:" + lookupInResult.contentAsObject(0));
      // end::kv-get-subdoc[]
    }

    {
      System.out.println("Example: [kv-update-replace]");
      // tag::kv-update-replace[]
      // Fetch an existing hotel document
      GetResult getResult = hotelCollection.get("hotel-123");
      JsonObject existingDoc = getResult.contentAsObject();

      // Get the current CAS value.
      Long currentCas = getResult.cas();
      System.out.println("Current CAS:" + currentCas);

      // Add a new review to the reviews array.
      existingDoc.getArray("reviews").add(JsonObject.create()
          .put("content", "This hotel was cozy, conveniently located and clean.")
          .put("author", "Carmella O'Keefe")
          .put("date", DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
      );

      // Update the document with new data and pass the current CAS value. 
      MutationResult replaceResult = hotelCollection.replace(
          "hotel-123",
          existingDoc,
          ReplaceOptions.replaceOptions().cas(currentCas)
      );

      // Print the new CAS value.
      System.out.println("New CAS:" + replaceResult.cas());
      // end::kv-update-replace[]
    }

    {
      System.out.println("Example: [kv-update-upsert]");
      // Create the document object.
      JsonObject document = JsonObject.create()
          .put("id", "hotel-123")
          .put("name", "Medway Youth Hostel")
          .put("address", "Capstone Road, ME7 3JE")
          .put("url", "http://www.yha.org.uk")
          .put("country", "United Kingdom")
          .put("city", "Medway")
          .put("state", (String) null)
          .put("vacancy", true)
          .put("description", "40 bed summer hostel about 3 miles from Gillingham.");

      // tag::kv-update-upsert[]
      // Update or create a document in the hotel collection.
      MutationResult upsertResult = hotelCollection.upsert("hotel-123", document);

      // Print the result's CAS metadata to the console.
      System.out.println("CAS:" + upsertResult.cas());
      // end::kv-update-upsert[]
    }

    {
      System.out.println("Example: [kv-update-subdoc]");
      // tag::kv-update-subdoc[]
      List<MutateInSpec> specs = Arrays.asList(MutateInSpec.upsert("pets_ok", true));

      MutateInResult mutateInResult = hotelCollection.mutateIn("hotel-123", specs);
      System.out.println("CAS:" + mutateInResult.cas());
      // end::kv-update-subdoc[]
    }
    
    {
      System.out.println("Example: [kv-remove-subdoc]");
      // tag::kv-remove-subdoc[]
      List<MutateInSpec> specs = Arrays.asList(MutateInSpec.remove("url"));

      MutateInResult mutateInResult = hotelCollection.mutateIn("hotel-123", specs);
      System.out.println("CAS:" + mutateInResult.cas());
      // end::kv-remove-subdoc[]
    }

    {
      System.out.println("Example: [kv-remove]");
      // tag::kv-remove[]
      MutationResult removeResult = hotelCollection.remove("hotel-123");
      System.out.println("CAS:" + removeResult.cas());
      // end::kv-remove[]
    }
  }
}
