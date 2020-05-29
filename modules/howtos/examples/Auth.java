import com.couchbase.client.core.env.CertificateAuthenticator;
import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;

import java.security.KeyStore;

import static com.couchbase.client.java.ClusterOptions.clusterOptions;

public class Auth {

  public static void main(String... args) {

    {
      // #tag::rbac-simple[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      // #end::rbac-simple[]
    }

    {
      // #tag::rbac-clusteroptions[]
      Cluster cluster = Cluster.connect(
        "127.0.0.1",
        clusterOptions("username", "password")
      );
      // #end::rbac-clusteroptions[]
    }

    {
      // #tag::rbac-pwd[]
      PasswordAuthenticator authenticator = PasswordAuthenticator.builder()
        .username("username")
        .password("password")
        // enables the PLAIN authentication mechanism, used with LDAP
        .enablePlainSaslMechanism()
        .build();

      Cluster cluster = Cluster.connect("127.0.0.1", clusterOptions(authenticator));
      // #end::rbac-pwd[]
    }

    {
      // #tag::certauth[]
      // should be replaced with your actual KeyStore
      KeyStore keyStore = loadKeyStore();

      CertificateAuthenticator authenticator = CertificateAuthenticator.fromKeyStore(
        keyStore,
        "keyStorePassword"
      );
      Cluster cluster = Cluster.connect("127.0.0.1", clusterOptions(authenticator));
      // #end::certauth[]
    }
  }

  private static KeyStore loadKeyStore() {
    return null;
  }

}
