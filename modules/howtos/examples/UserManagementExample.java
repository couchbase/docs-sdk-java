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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.user.Role;
import com.couchbase.client.java.manager.user.User;
import com.couchbase.client.java.manager.user.UserAndMetadata;
import com.couchbase.client.java.query.QueryResult;

import java.util.List;
import java.util.Set;

/**
 * Example of basic User Management
 *
 * @author Michael Reiche
 */
public class UserManagementExample {

  Cluster cluster;
  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";

  String bucketName = "travel-sample";
  String testUsername = "cbtestuser";
  String testPassword = "cbtestpassword";

  private void init() {
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
  }

  public void usermanagement_1() throws Exception {
    // tag::usermanagement_1[]
    User user = new User(testUsername).password(testPassword).displayName("Constance Lambert");
    user.roles(
        // Roles required for the reading of data from the bucket
        new Role("data_reader", "*"),
        new Role("query_select", "*"),
        // Roles required for the writing of data into the bucket.
        new Role("data_writer", bucketName),
        new Role("query_insert", bucketName),
        new Role("query_delete", bucketName),
        // Role required for the creation of indexes on the bucket.
        new Role("query_manage_index", bucketName));

    cluster.users().upsertUser(user);
    // end::usermanagement_1[]
  }

  public void usermanagement_2() throws Exception {

    // List current users.
    System.out.println("Listing current users.");
    // tag::usermanagement_2[]
    List<UserAndMetadata> listOfUsers = cluster.users().getAllUsers();
    for (int j = 0; j < listOfUsers.size(); j++) {
      UserAndMetadata currentUser = listOfUsers.get(j);
      System.out.println("User's display name is: " + currentUser.user().displayName() );
      Set<Role> currentRoles = currentUser.user().roles();
      for (Role role : currentRoles) {
        System.out.println("   User has the role: " + role.name() + ", applicable to bucket " + role.bucket() );
      }
    }
    // end::usermanagement_2[]
  }

  public void usermanagement_3() throws Exception {

    // Access the cluster that is running on the local host, specifying
    // the username and password already assigned by the administrator

    // tag::usermanagement_3[]
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    Cluster userCluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(testUsername, testPassword).environment(environment));
    Bucket userBucket = userCluster.bucket(bucketName);
    Scope scope = userBucket.scope("inventory");
    Collection collection = scope.collection("airline");

    cluster.queryIndexes().createPrimaryIndex(bucketName, // create index if needed
        CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));

    JsonObject returnedAirline10doc = collection.get("airline_10").contentAsObject();

    JsonObject airline11Object = JsonObject.create().put("callsign", "MILE-AIR").put("iata", "Q5").put("icao", "MLA")
        .put("id", 11).put("name", "40-Mile Air").put("type", "airline");

    collection.upsert("airline_11", airline11Object);

    JsonObject returnedAirline11Doc = collection.get("airline_11").contentAsObject();

    QueryResult result = userCluster.query("SELECT * FROM `travel-sample`.inventory.airline LIMIT 5");

    userCluster.disconnect();
    // end::usermanagement_3[]
    System.out.println("get -> "+returnedAirline11Doc);
    System.out.println("query -> ");
    for(JsonObject airline:result.rowsAsObject()){
      System.out.println("    "+airline);
    }
  }

  public void usermanagement_4() throws Exception {
    System.out.println("Removing user " + testUsername);
    // tag::usermanagement_4[]
    cluster.users().dropUser(testUsername);
    // end::usermanagement_4[]
    cluster.disconnect();
  }

  public static void main(String[] args) throws Exception {
    UserManagementExample obj = new UserManagementExample();
    obj.init();
    obj.usermanagement_1();
    obj.usermanagement_2();
    obj.usermanagement_3();
    obj.usermanagement_4();
    System.out.println("Done.");
  }
}
