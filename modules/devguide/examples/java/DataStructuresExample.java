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
import com.couchbase.client.java.datastructures.CouchbaseArrayList;
import com.couchbase.client.java.datastructures.CouchbaseArraySet;
import com.couchbase.client.java.datastructures.CouchbaseMap;
import com.couchbase.client.java.datastructures.CouchbaseQueue;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.kv.ArrayListOptions;
import com.couchbase.client.java.kv.ArraySetOptions;
import com.couchbase.client.java.kv.MapOptions;
import com.couchbase.client.java.kv.QueueOptions;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DataStructuresExample {

  String connectionString = "localhost";
  String username = "Administrator";
  String password = "password";
  String bucketName = "travel-sample";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;

  CouchbaseMap map;
  CouchbaseArrayList arrayList;
  CouchbaseArraySet arraySet;
  CouchbaseQueue queue;

  private void init() {
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
    scope = bucket.scope("tenant_agent_00");
    collection = scope.collection("users");
    // tag::data_structures_0[]
    map = new CouchbaseMap("mapId", collection, String.class, MapOptions.mapOptions());
    arrayList = new CouchbaseArrayList("arrayListId", collection, String.class, ArrayListOptions.arrayListOptions());
    arraySet = new CouchbaseArraySet("arraySetId", collection, String.class, ArraySetOptions.arraySetOptions());
    queue = new CouchbaseQueue("queueId", collection, String.class, QueueOptions.queueOptions());
    // end::data_structures_0[]
  }

  public void data_structures_1() throws Exception {
    // tag::data_structures_1[]
    map.put("some_key", "value");
    // end::data_structures_1[]
  }

  public void data_structures_2() throws Exception {
    // tag::data_structures_2[]
    map.remove("some_key");
    // end::data_structures_2[]
  }

  public void data_structures_3() throws Exception {
    // tag::data_structures_3[]
    map.get("some_key"); //=> value
    // end::data_structures_3[]
  }

  public void data_structures_4() throws Exception {
    // tag::data_structures_4[]
    arrayList.add(1234);
    // end::data_structures_4[]
  }

  public void data_structures_5() throws Exception {
    // tag::data_structures_5[]
    arrayList.add(0, "hello world");
    // end::data_structures_5[]
  }

  public void data_structures_6() throws Exception {
    // tag::data_structures_6[]
    arrayList.remove(1);
    // end::data_structures_6[]
  }

  public void data_structures_7() throws Exception {
    // tag::data_structures_7[]
    arrayList.set(0, "first value");
    // end::data_structures_7[]
  }

  public void data_structures_8() throws Exception {
    // tag::data_structures_8[]
    arrayList.get(0);
    // end::data_structures_8[]
  }

  public void data_structures_9() throws Exception {
    // tag::data_structures_9[]
    arraySet.add("some_value");
    // end::data_structures_9[]
  }

  public void data_structures_10() throws Exception {
    // tag::data_structures_10[]
    Set set = arraySet;
    // end::data_structures_10[]
  }

  public void data_structures_11() throws Exception {
    // tag::data_structures_11[]
    arraySet.contains("value");
    // end::data_structures_11[]
  }

  public void data_structures_12() throws Exception {
    // tag::data_structures_12[]
    arraySet.remove("some_value");
    // end::data_structures_12[]
  }

  public void data_structures_13() throws Exception {
    // tag::data_structures_13[]
    queue.add("job123");
    // end::data_structures_13[]
  }

  public void data_structures_14() throws Exception {
    // tag::data_structures_14[]
    Object item = queue.poll(); //=> "job123"
    // end::data_structures_14[]
  }

  public void data_structures_15() throws Exception {
    // tag::data_structures_15[]
    int len = arrayList.size(); //=> 42
    // end::data_structures_15[]
  }

  public void data_structures_16() throws Exception {
    // tag::data_structures_16[]
    Map<String, String> favorites = new CouchbaseMap<String>("mapDocId", collection, String.class, MapOptions.mapOptions());
    favorites.put("color", "Blue");
    favorites.put("flavor", "Chocolate");

    System.out.println(favorites); //=> {flavor=Chocolate, color=Blue}

    // What does the JSON document look like?
    System.out.println(collection.get("mapDocId").contentAsObject());
    //=> {"flavor":"Chocolate","color":"Blue"}
    // end::data_structures_16[]
  }

  public void data_structures_17() throws Exception {
    // tag::data_structures_17[]
    List<String> names = new CouchbaseArrayList<String>("listDocId", collection, String.class, ArrayListOptions.arrayListOptions());
    names.add("Alice");
    names.add("Bob");
    names.add("Alice");

    System.out.println(names); //=> [Alice, Bob, Alice]

    // What does the JSON document look like?
    System.out.println(collection.get("listDocId").contentAsArray());
    //=> ["Alice","Bob","Alice"]
    // end::data_structures_17[]
  }

  public void data_structures_18() throws Exception {
    // tag::data_structures_18[]
    Set<String> uniqueNames = new CouchbaseArraySet<String>("setDocId", collection, String.class, ArraySetOptions.arraySetOptions());
    uniqueNames.add("Alice");
    uniqueNames.add("Bob");
    uniqueNames.add("Alice");

    System.out.println(uniqueNames); //=> [Alice, Bob]

    // What does the JSON document look like?
    System.out.println(collection.get("setDocId").contentAsArray());
    //=> ["Alice","Bob"]
    // end::data_structures_18[]
  }

  public void data_structures_19() throws Exception {
    // tag::data_structures_19[]
    Queue<String> shoppingList = new CouchbaseQueue<String>("queueDocId", collection, String.class, QueueOptions.queueOptions());
    shoppingList.add("loaf of bread");
    shoppingList.add("container of milk");
    shoppingList.add("stick of butter");

    // What does the JSON document look like?
    System.out.println(collection.get("queueDocId").contentAsArray());
    //=> ["stick of butter","container of milk","loaf of bread"]

    String item;
    while ((item = shoppingList.poll()) != null) {
      System.out.println(item);
      // => loaf of bread
      // => container of milk
      // => stick of butter
    }

    // What does the JSON document look like after draining the queue?
    System.out.println(collection.get("queueDocId").contentAsArray());
    //=> []
    // end::data_structures_19[]
  }

  public static void main(String[] args) throws Exception {
    DataStructuresExample obj = new DataStructuresExample();
    obj.init();
    obj.data_structures_1();
    obj.data_structures_2();
    obj.data_structures_3();
    obj.data_structures_4();
    obj.data_structures_5();
    obj.data_structures_6();
    obj.data_structures_7();
    obj.data_structures_8();
    obj.data_structures_9();
    obj.data_structures_10();
    obj.data_structures_11();
    obj.data_structures_12();
    obj.data_structures_13();
    obj.data_structures_14();
    obj.data_structures_15();
    obj.data_structures_16();
    obj.data_structures_17();
    obj.data_structures_18();
    obj.data_structures_19();
    System.out.println("Done.");
  }
}
