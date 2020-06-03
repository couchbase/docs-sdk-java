import com.couchbase.client.core.error.CasMismatchException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.ReplaceOptions;

import java.time.Duration;

import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;

public class Cas {

  public static void main(String... args) {


    Collection collection = null;

    {
      // #tag::handlingerrors[]
      int maxRetries = 10;

      for (int i = 0; i < maxRetries; i++) {
        // Get the current document contents
        GetResult getResult = collection.get("user-id");

        // Increment a count on the user
        JsonObject content = getResult.contentAsObject();
        content.put("visitCount", content.getLong("visitCount") + 1);

        try {
          // Attempt to replace the document with cas
          collection.replace("user-id", content, replaceOptions().cas(getResult.cas()));
          break;
        } catch (CasMismatchException ex) {
          // continue the loop on cas mismatch to try again
          // note that any other exception will be raised and break the loop as well
        }
      }
      // #end::handlingerrors[]
    }

    {
      // #tag::locking[]
      GetResult getAndLockResult = collection.getAndLock("key", Duration.ofSeconds(2));

      long lockedCas = getAndLockResult.cas();

      /* an example of simply unlocking the document:
      collection.unlock("key", lockedCas);
       */

      collection.replace("key", "new value", replaceOptions().cas(lockedCas));
      // #end::locking[]
    }
  }

}
