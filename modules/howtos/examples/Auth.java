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

import com.couchbase.client.core.env.CertificateAuthenticator;
import com.couchbase.client.core.env.PasswordAuthenticator;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import java.util.EnumSet;
import com.couchbase.client.core.env.SaslMechanism;

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
      Cluster cluster = Cluster.connect("127.0.0.1", clusterOptions("username", "password"));
      // #end::rbac-clusteroptions[]
    }

    {
      // #tag::rbac-pwd[]
      PasswordAuthenticator authenticator = PasswordAuthenticator
        .builder()
        .username("username")
        .password("password")
        // enables only the PLAIN authentication mechanism, used with LDAP
        .onlyEnablePlainSaslMechanism()
        .build();

      Cluster cluster = Cluster.connect("127.0.0.1", clusterOptions(authenticator));
      // #end::rbac-pwd[]
    }

    {
      // #tag::certauth[]
      // should be replaced with your actual KeyStore
      KeyStore keyStore = loadKeyStore();

      CertificateAuthenticator authenticator = CertificateAuthenticator.fromKeyStore(keyStore, "keyStorePassword");
      Cluster cluster = Cluster.connect("127.0.0.1", clusterOptions(authenticator));
      // #end::certauth[]
    }
  }

  private static KeyStore loadKeyStore() {
    return null;
  }

}
