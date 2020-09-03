
import com.couchbase.client.core.cnc.Event;
import com.couchbase.client.core.diagnostics.PingResult;
import com.couchbase.client.core.encryption.CryptoManager;
import com.couchbase.client.core.endpoint.CircuitBreakerConfig;
import com.couchbase.client.core.env.Authenticator;
import com.couchbase.client.core.env.CompressionConfig;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.LoggerConfig;
import com.couchbase.client.core.env.NetworkResolution;
import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.core.env.SeedNode;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.logging.LogRedaction;
import com.couchbase.client.core.logging.RedactionLevel;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.core.retry.BestEffortRetryStrategy;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.encryption.AeadAes256CbcHmacSha512Provider;
import com.couchbase.client.encryption.DefaultCryptoManager;
import com.couchbase.client.encryption.KeyStoreKeyring;
import com.couchbase.client.encryption.Keyring;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.codec.TypeRef;
import com.couchbase.client.java.datastructures.CouchbaseArrayList;
import com.couchbase.client.java.datastructures.CouchbaseArraySet;
import com.couchbase.client.java.datastructures.CouchbaseMap;
import com.couchbase.client.java.datastructures.CouchbaseQueue;
import com.couchbase.client.java.diagnostics.PingOptions;
import com.couchbase.client.java.encryption.annotation.Encrypted;
import com.couchbase.client.java.encryption.databind.jackson.EncryptionModule;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.json.JsonObjectCrypto;
import com.couchbase.client.java.json.JsonValueModule;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.kv.LookupInMacro;
import com.couchbase.client.java.kv.LookupInSpec;
import com.couchbase.client.java.kv.MutateInSpec;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.MutationState;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.CreateQueryIndexOptions;
import com.couchbase.client.java.manager.query.QueryIndexManager;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.result.SearchResult;
import com.couchbase.client.java.search.result.SearchRow;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.internal.org.objectweb.asm.commons.Remapper;
import jdk.nashorn.internal.runtime.logging.DebugLogger;

public class managing_connections {

  private static final String FLIGHTS_COLLECTION_NAME = "flightsCollection";
  private static final String USERS_COLLECTION_NAME = "usersCollection";

  String connectionString="localhost";
  String username="Administrator";
  String password="password";
  String id = "user::" + UUID.randomUUID().toString();
  DurabilityLevel expiry;

  Cluster cluster;
 Bucket bucket;
 Scope scope;
 Collection collection;

 BucketSettings bucketSettings;

  CouchbaseMap map;
  CouchbaseArrayList arrayList;
  CouchbaseArraySet arraySet;
  CouchbaseQueue queue;	
  private void init(){
    // #tag::connection_1[];
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,      ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket("travel-sample");
    scope = bucket.defaultScope();    collection = bucket.defaultCollection();    // #end::connection_1[];
  }
  public void managing_connections_5() throws Exception { // file: howtos/pages/managing-connections.adoc line: 125
    // #tag::managing_connections_5[]
    int customKvPort = 1234;
    int customManagerPort = 2345;
    Set<SeedNode> seedNodes = new HashSet<>(Arrays.asList(
      SeedNode.create("127.0.0.1",
          Optional.of(customKvPort),
          Optional.of(customManagerPort))));
    
    Authenticator authenticator = PasswordAuthenticator.create(username, password);
    ClusterOptions options = ClusterOptions.clusterOptions(authenticator);
    Cluster cluster = Cluster.connect(seedNodes, options);
    // #end::managing_connections_5[];
  }

  public void managing_connections_8() throws Exception { // file: howtos/pages/managing-connections.adoc line: 242
    // #tag::managing_connections_8[]
    Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
    cluster.waitUntilReady(Duration.ofSeconds(10));
    Bucket bucket = cluster.bucket("travel-sample");
    Collection collection = bucket.defaultCollection();
    // #end::managing_connections_8[];
  }

  public void managing_connections_9() throws Exception { // file: howtos/pages/managing-connections.adoc line: 252
    // #tag::managing_connections_9[]
        Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
        cluster.waitUntilReady(Duration.ofSeconds(10));
        Bucket bucket = cluster.bucket("travel-sample");
        Collection collection = bucket.defaultCollection();
    // #end::managing_connections_9[];
  }

  public static void main(String[] args) throws Exception{
    managing_connections obj = new managing_connections();
    obj.init();
    obj.managing_connections_5();
    obj.managing_connections_8();
    obj.managing_connections_9();
  }
}
