import com.couchbase.client.core.encryption.CryptoManager;
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
import com.couchbase.client.java.encryption.annotation.Encrypted;
import com.couchbase.client.java.encryption.databind.jackson.EncryptionModule;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.json.JsonObjectCrypto;
import com.couchbase.client.java.json.JsonValueModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

public class encrypting_using_sdk {

  String connectionString="localhost";
  String username="Administrator";
  String password="password";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;

  private void init(){
    // #tag::connection_1[];
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,      ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket("travel-sample");
    scope = bucket.defaultScope();    collection = bucket.defaultCollection();    // #end::connection_1[];
  }
  public void encrypting_using_sdk_1() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 60
    // #tag::encrypting_using_sdk_1[]
    KeyStore javaKeyStore = KeyStore.getInstance("MyKeyStoreType");
    FileInputStream fis = new java.io.FileInputStream("keyStoreName");
    char[] password = { 'a', 'b', 'c'};
    javaKeyStore.load(fis, password);
    Keyring keyring = new KeyStoreKeyring(javaKeyStore, keyName -> "swordfish");
    
    // AES-256 authenticated with HMAC SHA-512. Requires a 64-byte key.
    AeadAes256CbcHmacSha512Provider provider = AeadAes256CbcHmacSha512Provider.builder()
        .keyring(keyring)
        .build();
    
    CryptoManager cryptoManager = DefaultCryptoManager.builder()
        .decrypter(provider.decrypter())
        .defaultEncrypter(provider.encrypterForKey("myKey"))
        .build();
    
    ClusterEnvironment env = ClusterEnvironment.builder()
        .cryptoManager(cryptoManager)
        .build();
    
    Cluster cluster = Cluster.connect("localhost",
        ClusterOptions.clusterOptions("username", "password")
            .environment(env));
    // #end::encrypting_using_sdk_1[];
  }

    // file: howtos/pages/encrypting-using-sdk.adoc line: 97
    // #tag::encrypting_using_sdk_2[]
    public class Employee {
      @Encrypted
      private boolean replicant;
    
      // alternatively you could annotate the getter or setter
      public boolean isReplicant() {
        return replicant;
      }
    
      public void setReplicant(boolean replicant) {
        this.replicant = replicant;
      }
    }
    // #end::encrypting_using_sdk_2[]
  public void encrypting_using_sdk_3() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 116
    // #tag::encrypting_using_sdk_3[]
    Collection collection = cluster.bucket("myBucket")
        .defaultCollection();
    
    Employee employee = new Employee();
    employee.setReplicant(true);
    collection.upsert("employee:1234", employee);
    // #end::encrypting_using_sdk_3[];
  }

  public void encrypting_using_sdk_4() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 128
    // #tag::encrypting_using_sdk_4[]
    JsonObject encrypted = collection.get("employee:1234")
        .contentAsObject();
    
    System.out.println(encrypted);
    // #end::encrypting_using_sdk_4[];
  }

  public void encrypting_using_sdk_5() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 152
    // #tag::encrypting_using_sdk_5[]
    Employee readItBack = collection.get("employee:1234")
        .contentAs(Employee.class);
    
    System.out.println(readItBack.isReplicant());
    // #end::encrypting_using_sdk_5[];
  }

  public void encrypting_using_sdk_6() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 173
    // #tag::encrypting_using_sdk_6[]
    // CryptoManager cryptoManager = createMyCryptoManager();
    KeyStore javaKeyStore = KeyStore.getInstance("MyKeyStoreType");
    FileInputStream fis = new java.io.FileInputStream("keyStoreName");
    char[] ksPassword = { 'a', 'b', 'c'};
    javaKeyStore.load(fis, ksPassword);
    Keyring keyring = new KeyStoreKeyring(javaKeyStore, keyName -> "swordfish");

    // AES-256 authenticated with HMAC SHA-512. Requires a 64-byte key.
    AeadAes256CbcHmacSha512Provider provider = AeadAes256CbcHmacSha512Provider.builder()
        .keyring(keyring)
        .build();
    CryptoManager cryptoManager = DefaultCryptoManager.builder()
        .decrypter(provider.decrypter())
        .defaultEncrypter(provider.encrypterForKey("myKey"))
        .build();
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonValueModule()); // for JsonObject
    mapper.registerModule(new EncryptionModule(cryptoManager));
    
    // Here you can register more modules, add mixins, enable features, etc.
    
    ClusterEnvironment env = ClusterEnvironment.builder()
        .cryptoManager(cryptoManager)
        .jsonSerializer(JacksonJsonSerializer.create(mapper))
        .build();
    
    Cluster cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password)
            .environment(env));
    // #end::encrypting_using_sdk_6[];
  }

  public void encrypting_using_sdk_7() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 204
    // #tag::encrypting_using_sdk_7[]
    Collection collection = cluster.bucket("myBucket").defaultCollection();
    
    JsonObject document = JsonObject.create();
    JsonObjectCrypto crypto = document.crypto(collection);
    
    crypto.put("locationOfBuriedTreasure", "Between palm trees");
    
    // This displays the encrypted form of the field
    System.out.println(document);
    
    collection.upsert("treasureMap", document);
    
    JsonObject readItBack = collection.get("treasureMap").contentAsObject();
    JsonObjectCrypto readItBackCrypto = crypto.withObject(readItBack);
    System.out.println(readItBackCrypto.getString("locationOfBuriedTreasure"));
    // #end::encrypting_using_sdk_7[];
  }

  public void encrypting_using_sdk_8() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 231
    // #tag::encrypting_using_sdk_8[]
    KeyStore keyStore = KeyStore.getInstance("JCEKS");
    keyStore.load(null); // initialize new empty key store
    
    // Generate 64 random bytes
    SecureRandom random = new SecureRandom();
    byte[] keyBytes = new byte[64];
    random.nextBytes(keyBytes);
    
    // Add a new key called "my-key" to the key store
    KeyStoreKeyring.setSecretKey(keyStore, "my-key", keyBytes,
        "protection-password".toCharArray());
    
    // Write the key store to disk
    try (OutputStream os = new FileOutputStream("MyKeystoreFile.jceks")) {
      keyStore.store(os, "integrity-password".toCharArray());
    }
    // #end::encrypting_using_sdk_8[];
  }

  public void encrypting_using_sdk_9() throws Exception { // file: howtos/pages/encrypting-using-sdk.adoc line: 253
    // #tag::encrypting_using_sdk_9[]
    KeyStore keyStore = KeyStore.getInstance("JCEKS");
    try (InputStream is = new FileInputStream("MyKeystoreFile.jceks")) {
      keyStore.load(is, "integrity-password".toCharArray());
    }
    
    KeyStoreKeyring keyring = new KeyStoreKeyring(
        keyStore, keyName -> "protection-password");
    // #end::encrypting_using_sdk_9[];
  }

  public static void main(String[] args) throws Exception{
    encrypting_using_sdk obj = new encrypting_using_sdk();
    obj.init();
    obj.encrypting_using_sdk_1();
    obj.encrypting_using_sdk_3();
    obj.encrypting_using_sdk_4();
    obj.encrypting_using_sdk_5();
    obj.encrypting_using_sdk_6();
    obj.encrypting_using_sdk_7();
    obj.encrypting_using_sdk_8();
    obj.encrypting_using_sdk_9();
    System.out.println("Done.");
  }
}
