import static com.couchbase.client.java.analytics.AnalyticsOptions.analyticsOptions;
import static com.couchbase.client.java.kv.GetOptions.getOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;

import java.util.concurrent.CompletableFuture;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.core.msg.Request;
import com.couchbase.client.core.msg.Response;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.core.retry.RetryAction;
import com.couchbase.client.core.retry.RetryReason;
import com.couchbase.client.core.retry.RetryStrategy;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.analytics.AnalyticsResult;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class ErrorHandling {

  public static void main(String... args) {
    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
    Bucket bucket = cluster.bucket("travel-sample");
    Collection collection = bucket.collection("travel-sample");

    // tag::readonly[]
    QueryResult queryResult = cluster.query("SELECT * FROM bucket", queryOptions().readonly(true));

    AnalyticsResult analyticsResult = cluster.analyticsQuery("SELECT * FROM dataset",
        analyticsOptions().readonly(true));
    // end::readonly[]

    {
      // tag::getfetch[]
      // This will raise a `CouchbaseException` and propagate it
      GetResult result = collection.get("my-document-id");

      // Rethrow with a custom exception type
      try {
        collection.get("my-document-id");
      } catch (CouchbaseException ex) {
        throw new DatabaseException("Couchbase lookup failed", ex);
      }
      // end::getfetch[]
    }

    {
      // tag::getcatch[]
      try {
        collection.get("my-document-id");
      } catch (DocumentNotFoundException ex) {
        createDocument("my-document-id");
      } catch (CouchbaseException ex) {
        throw new DatabaseException("Couchbase lookup failed", ex);
      }
      // end::getcatch[]
    }

    {
      // tag::tryupsert[]
      for (int i = 0; i < 10; i++) {
        try {
          collection.upsert("docid", JsonObject.create().put("my", "value"));
          break;
        } catch (TimeoutException ex) {
          // propagate, since time budget's up
          break;
        } catch (CouchbaseException ex) {
          System.err.println("Failed: " + ex + ", retrying.");
          // don't break, so retry
        }
      }
      // end::tryupsert[]
    }

    {
      RetryStrategy myCustomStrategy = null;
      // tag::customglobal[]
      ClusterEnvironment environment = ClusterEnvironment.builder().retryStrategy(myCustomStrategy).build();
      // end::customglobal[]
      environment.shutdown();
    }

    {
      RetryStrategy myCustomStrategy = null;
      // tag::customreq[]
      collection.get("doc-id", getOptions().retryStrategy(myCustomStrategy));
      // end::customreq[]
    }

    {
      // tag::reactivesub[]
      collection.reactive().get("this-doc-does-not-exist").subscribe(new Subscriber<GetResult>() {

        @Override
        public void onError(Throwable throwable) {
          // This method will be called with a DocumentNotFoundException
        }

        @Override
        public void onSubscribe(Subscription subscription) {
        }

        @Override
        public void onNext(GetResult getResult) {
        }

        @Override
        public void onComplete() {
        }
      });
      // end::reactivesub[]
    }

    {
      // tag::reactivefallback[]
      Mono<JsonObject> documentContent = collection.reactive().get("my-doc-id").map(GetResult::contentAsObject)
          .onErrorResume(DocumentNotFoundException.class, e -> createDocumentReactive("my-doc-id"));
      // end::reactivefallback[]
    }

    {
      // tag::reactiveretry[]
      collection.reactive().get("my-doc-id")
          .retryWhen(Retry.max(5).filter(t -> t instanceof DocumentNotFoundException));
      // end::reactiveretry[]
    }
  }

  private static void createDocument(String id) {

  }

  private static Mono<JsonObject> createDocumentReactive(String id) {
    return Mono.empty();
  }

  // tag::customclass[]
  class MyCustomRetryStrategy extends BestEffortRetryStrategy {

    @Override
    public CompletableFuture<RetryAction> shouldRetry(Request<? extends Response> request, RetryReason reason) {
      // ---
      // Custom Logic Here
      // ---

      // Do not forget to call super at the end as fallback!
      return super.shouldRetry(request, reason);
    }
  }
  // end::customclass[]

  // tag::failfastcircuit[]
  class MyCustomRetryStrategy2 extends BestEffortRetryStrategy {
    @Override
    public CompletableFuture<RetryAction> shouldRetry(Request<? extends Response> request, RetryReason reason) {
      if (reason == RetryReason.ENDPOINT_CIRCUIT_OPEN) {
        return CompletableFuture.completedFuture(RetryAction.noRetry());
      }

      return super.shouldRetry(request, reason);
    }
  }
  // end::failfastcircuit[]

  static class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
      super(cause);
    }
  }
}
