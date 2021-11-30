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

// Couchbase SDK
import com.couchbase.client.java.*;
import com.couchbase.client.java.kv.*;

// tag::jsonobject-imports[]
import com.couchbase.client.java.json.*;
// end::jsonobject-imports[]

import com.couchbase.client.java.query.*;

// Java helpers for data-structure construction
import java.util.Map;
import java.util.List;
import java.util.Arrays;

// tag::file-imports[]
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
// end::file-imports[]

// tag::object-mapper-imports[]
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.codec.JacksonJsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
// end::object-mapper-imports[]

// tag::file-raw-imports[]
import com.couchbase.client.java.codec.RawJsonTranscoder;
// end::file-raw-imports[]

// tag::time-imports[]
import java.time.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonFormat;
// end::time-imports[]

// tag::identify-imports[]
import com.fasterxml.jackson.databind.JsonNode;
// end::identify-imports[]


public class Json {

  static String connectionString = "localhost";
  static String username = "Administrator";
  static String password = "password";
  static String bucketName = "travel-sample";
  
  static // nested static class for this example
  // tag::person-class[]
  public class Person {
    public String name;
    
    public int number;
    
    public Person() {} // default constructor needed to deserialize
    
    public Person(String name, int number) {
      this.name = name;
      this.number = number;
    }
  }
  // end::person-class[]
  
  static // nested static class for this example
  // tag::persona-class[]
  public class Persona {
    @JsonProperty("name")
    public String nombre;
    
    @JsonProperty("number")
    public int numero;
    
    // ...
    // tag::exclude[]
    public Persona() {} // default constructor needed to deserialize
  
    public Persona(String nombre, int numero) {
      this.nombre = nombre;
      this.numero = numero;
    }
    // end::exclude[]
  }
  // end::persona-class[]
  
  static // nested static class for this example
  // tag::event-class[]
  public  class Event {
    public String name;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime date;
    
    // ...
    // tag::exclude[]
    public Event() {} // default constructor needed to deserialize
    
    public Event(String name, LocalDateTime date) {
      this.name = name;
      this.date = date;
    }
    // end::exclude[]
  }
  // end::event-class[]
  
  public static void main(String... args) {
    // tag::object-mapper[]
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonValueModule()); // for JsonObject
    // tag::object-mapper-dates[]
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // end::object-mapper-dates[]

    ClusterEnvironment env = 
      ClusterEnvironment.builder()
        .jsonSerializer(JacksonJsonSerializer.create(mapper))
        .build();

    Cluster cluster = Cluster.connect(
      connectionString,
      ClusterOptions
        .clusterOptions(username, password)
        .environment(env));
    // end::object-mapper[]

    System.out.println(cluster.environment().jsonSerializer().getClass());
    // com.couchbase.client.java.codec.JacksonJsonSerializer
    // https://docs.couchbase.com/sdk-api/couchbase-java-client/com/couchbase/client/java/codec/JacksonJsonSerializer.html

    Bucket bucket = cluster.bucket(bucketName);
    Scope scope = bucket.scope("tenant_agent_00");
    Collection collection = scope.collection("users");
    
    {
      /* tag::arthur[]
      { 
        name: "Arthur",
        number: 42
      }
      // end::arthur[] */
      
      // tag::jsonobject[]   
      JsonObject json = JsonObject.create()
        .put("name", "Arthur")
        .put("number", 42);
      // end::jsonobject[]
        
      // tag::upsert[]
      collection.upsert("arthur", json);
      // end::upsert[]
    
      // tag::retrieve-json[]
      JsonObject jsonResult = collection.get("arthur")
        .contentAsObject();
      
      System.out.println(jsonResult);
      System.out.println(jsonResult.getString("name"));
      // end::retrieve-json[]
    }
    
    {
      // tag::map[]
      Map<String, Object> map = Map.of( // Java 9+ syntax
        "name", "Arthur",
        "number", 42);
      
      JsonObject json = JsonObject.from(map);
      // end::map[]
      
      collection.upsert("arthur", json);
      
      // tag::map-insert[]
      collection.upsert("arthur", map);
      // end::map-insert[]

      // tag::retrieve-map[]
      Map<String, Object> result = collection.get("arthur")
        .contentAs(Map.class);
      // end::retrieve-map[]
          
        System.out.println(result);
    }
    
    {
      try {
        String pathToArthurJson = "howtos/examples/arthur.json";
        // tag::file[]
        String content  = new String(
          Files.readAllBytes(
            Paths.get(pathToArthurJson)),
          StandardCharsets.UTF_8);
        // String content  = Files.readString(Paths.get(pathToArthurJson), StandardCharsets.UTF_8); // Java 11+

        JsonObject json = JsonObject.fromJson(content);
        // end::file[]
        collection.upsert("arthur", json);

        // tag::file-raw[]
        collection.upsert("arthur", content,
          UpsertOptions.upsertOptions().transcoder(RawJsonTranscoder.INSTANCE));
        // end::file-raw[]
      }
      catch (IOException e) {
        System.out.println(e);
      }
    }
    
    {
      // tag::top-level-null-workaround[]
      // see https://issues.couchbase.com/browse/JCBC-1723
      collection.upsert("some-nullable-key", "null",
        UpsertOptions.upsertOptions().transcoder(RawJsonTranscoder.INSTANCE));
      // end::top-level-null-workaround[]
    }
    
    {
      // tag::insert-person[]
      collection.upsert("arthur", new Person("Arthur", 42));
      // end::insert-person[]

      // tag::get-person[]
      Person person = collection.get("arthur")
        .contentAs(Person.class);
      // end::get-person[]

      System.out.println(person);
      
      JsonObject json = collection.get("person1")
        .contentAsObject();
      System.out.println(json);
    }
    
    {
      collection.upsert("person2", new Persona("Artur", 42));
      
      Persona person = collection.get("person2")
        .contentAs(Persona.class);
      System.out.println(person);
      
      JsonObject json = collection.get("person2")
        .contentAsObject();
      System.out.println(json);
    }
    
    {
      // tag::nested[]
      JsonObject arthur = JsonObject.create()
        .put("name", "Arthur")
        .put("number", 42)
        .put("float", 42.0)
        .put("address", JsonObject.create()
          .put("street", "Country Lane")
          .put("number", 155)
          .put("town", "Cottington"))
        .put("harmless", true)
        .put("tea", JsonNull.INSTANCE) // or e.g. (String) null
        .put("possessions",
              JsonArray.from(
                "dressing gown",
                "pyjamas"));
      // end::nested[]

      collection.upsert("arthur-nested", arthur);
    }
    
    {
      // tag::string[]
      collection.upsert("some-string", "string-value");
      // end::string[]

      // tag::string-get[]
      System.out.println(
        collection.get("some-string")
          .contentAs(String.class));
      // end::string-get[]

      collection.upsert("integer", 123);
      System.out.println(
        collection.get("integer")
          .contentAs(Integer.class));

      collection.upsert("boolean", true);
      GetResult result = collection.get("boolean");
      Boolean bool = result.contentAs(Boolean.class);
      System.out.println(bool); // true
    }
    
    {
      // tag::event[]
      Event towelday = new Event("Towel Day",
        LocalDateTime.of(2021, Month.MAY, 25, 9, 30));
      
      collection.upsert("towel-day", towelday);
      
      Event event = collection.get("towel-day").contentAs(Event.class);
      System.out.println(event.date);
      // end::event[]
    }
    
    {
      JsonArray numbers = JsonArray.create().add(1).add(2).add(3); // ...
      
      List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
      // or List.of(...) in Java 9+
      
      collection.upsert("array1", numbers);
      collection.upsert("array2", list);
      collection.upsert("array3", JsonArray.from(list));
    }
    
    {
      // tag::subdoc-simple[]
      String string = "value";
      collection.mutateIn("arthur",
        Arrays.asList(MutateInSpec.upsert("key", string)));
      // end::subdoc-simple[]

      /* tag::subdoc-simple-json[]     
      { "name": "Arthur",
        "number": 42,
        "key": "value" }
      // end::subdoc-simple-json[] */
      
      // tag::subdoc-object[]
      JsonObject object  = JsonObject.create().put("subkey", "subvalue");
      collection.mutateIn("arthur",
        Arrays.asList(MutateInSpec.upsert("key", object)));
      // end::subdoc-object[]

      /* tag::subdoc-object-json[]     
      { "name": "Arthur",
        "number": 42,
        "key": {
          "subkey":"subvalue"
        }
      }
      // end::subdoc-object-json[] */
        
      System.out.println(
        collection.get("arthur").contentAsObject());
    }
    
    {
      collection.upsert("mystery", "spooky");
      
      // tag::identify[]
      GetResult result = collection.get("mystery");
      
      JsonNode node = result.contentAs(JsonNode.class);
      if (node.isBoolean()) {
        System.out.println("It's a boolean!");
        if (node.booleanValue()) {
          System.out.println("SUCCESS!");
        }
      }
      else {
        System.out.println(node.getNodeType());
        // STRING
      }
      // end::identify[]
    }
  }
}
