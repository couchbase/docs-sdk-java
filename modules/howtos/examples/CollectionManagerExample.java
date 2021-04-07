/*
 * Copyright (c) 2021 Couchbase, Inc.
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

// tag::imports[]
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;

import com.couchbase.client.java.manager.user.UserManager;
import com.couchbase.client.java.manager.user.User;
import com.couchbase.client.java.manager.user.Role;

import com.couchbase.client.core.error.ScopeExistsException;
import com.couchbase.client.core.error.CollectionExistsException;
import com.couchbase.client.core.error.ScopeNotFoundException;
import com.couchbase.client.core.error.CollectionNotFoundException;
import com.couchbase.client.core.error.AuthenticationFailureException;
// end::imports[]

public class CollectionManagerExample {

  static UserManager users = Cluster.connect("localhost", "Administrator", "password").users();

  static CollectionManager getCollectionManager (String username, String password) {

    System.out.println("create-collection-manager");

    // tag::create-collection-manager[]
    Cluster cluster = Cluster.connect("localhost", username, password);
    Bucket bucket = cluster.bucket("travel-sample");
    CollectionManager collectionMgr = bucket.collections();
    // end::create-collection-manager[]

    return collectionMgr;
  }

  public static void main(String... args) {
    {
      {
        System.out.println("bucketAdmin");

        // tag::bucketAdmin[]
        users.upsertUser(
          new User("bucketAdmin")
          .password("password")
          .displayName("Bucket Admin [travel-sample]")
          .roles(
            new Role("bucket_admin", "travel-sample")));
        // end::bucketAdmin[]
      }

      {
        System.out.println("create-scope");
        CollectionManager collectionMgr = getCollectionManager("bucketAdmin", "password");

        // tag::create-scope[]
        try {
          collectionMgr.createScope("example-scope");
        }
        catch (ScopeExistsException e) {
          System.out.println("Scope already exists");
        }
        // end::create-scope[]
      }

      {
        System.out.println("scopeAdmin");

        // tag::scopeAdmin[]
        users.upsertUser(
          new User("scopeAdmin")
          .password("password")
          .displayName("Manage Collections in Scope [travel-sample:*]")
          .roles(
            new Role("scope_admin", "travel-sample", "example-scope", "*"),
            new Role("data_reader", "travel-sample", "*", "*")));
        // end::scopeAdmin[]
      }

      {
        System.out.println("create-collection");
        CollectionManager collectionMgr = getCollectionManager("scopeAdmin", "password");

        // tag::create-collection[]
        CollectionSpec spec = CollectionSpec.create("example-collection", "example-scope");

        try {
          collectionMgr.createCollection(spec);
        }
        catch (CollectionExistsException e) {
          System.out.println("Collection already exists");
        }
        catch (ScopeNotFoundException e) {
          System.out.println("The specified parent scope doesn't exist");
        }
        // end::create-collection[]

        System.out.println("drop-collection");
        // tag::drop-collection[]
        try {
          collectionMgr.dropCollection(spec);
        }
        catch (CollectionNotFoundException e) {
          System.out.println("The specified collection doesn't exist");
        }
        catch (ScopeNotFoundException e) {
          System.out.println("The specified parent scope doesn't exist");
        }
        // end::drop-collection[]
      }

      {
        System.out.println("drop-scope");
        CollectionManager collectionMgr = getCollectionManager("bucketAdmin", "password");

        // tag::drop-scope[]
        try {
          collectionMgr.dropScope("example-scope");
        }
        catch (ScopeNotFoundException e) {
          System.out.println("The specified scope doesn't exist");
        }
        // end::drop-scope[]
      }
    }

  }

}
