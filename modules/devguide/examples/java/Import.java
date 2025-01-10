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

// tag::json-jsonl-import[]
import com.couchbase.client.java.json.*;
// end::json-jsonl-import[]

// Java helpers for data-structure construction
import java.util.Map;
import java.util.List;

// tag::file-imports[]
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.FileReader;
import java.io.BufferedReader;
// end::file-imports[]

// tag::csv-tsv-import[]
import com.opencsv.*;
// end::csv-tsv-import[]

import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;

import reactor.core.publisher.Flux;
import java.util.stream.BaseStream;
import java.util.HashMap;
import java.time.Duration;

public class Import {

  static String connectionString = "localhost";
  static String username = "Administrator";
  static String password = "password";
  static String bucketName = "travel-sample";

  private Collection collection;
  private ReactiveCollection reactiveCollection;
  
  public Import() {
    // tag::connect[]
    Cluster cluster = Cluster.connect(
      connectionString,
      ClusterOptions
        .clusterOptions(username, password));
    
    Bucket bucket = cluster.bucket(bucketName);
    Scope scope = bucket.scope("inventory");
    
    collection = scope.collection("airline");
    // end::connect[]
    
    // tag::reactiveCollection[]
    reactiveCollection = collection.reactive();
    // end::reactiveCollection[]
  }
  
  public static void main(String... args) {
    Import importer = new Import();
    
    importer.importCSV();
    importer.importTSV();
    importer.importJSON();
    importer.importJSONL();
    importer.importCSV_batch();
    importer.importTSV_batch();
    importer.importJSON_batch();
    importer.importJSONL_batch();
  }
  
  // NOTE: non-generic Map, because the readers produce slightly different outputs:
  //   CSV: Map<String,String>
  //   JSON: Map<String,Object>
  // and as generics annotations are lost at runtime we can't have
  // a single overloaded method to deal with those.
  //
  // In real code, you might choose to cast from Object to Map<String,Object>
  // or define multiple methods, but those confuse the example a little.
  
  // tag::preprocess[]
  public JsonDocument preprocess(Map row) {
    Map value = new HashMap(row);

    // define the KEY
    String key = value.get("type") + "_" + value.get("id");

    // do any additional processing
    value.put("importer", "Java SDK");
    
    return new JsonDocument(key, JsonObject.from(value));
  }
  // end::preprocess[]
  
  // tag::upsertDocument[]
  public void upsertRow(Map row) {
    
    JsonDocument doc = preprocess(row);
    
    String key = doc.getId();
    Object value = doc.getContent();
    
    // upsert the document
    collection.upsert(key, value);
    
    // any required logging
    System.out.println(key);
    System.out.println(value);
  }
  // end::upsertDocument[]
  
  
  // tag::importCSV[]
  public void importCSV() {
    try (CSVReaderHeaderAware csv = new CSVReaderHeaderAware(
        new FileReader("modules/howtos/examples/import.csv"))) {
            
      Map<String, String> row;
      while ((row = csv.readMap()) != null) {
        upsertRow(row);
      }
    }
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    catch (com.opencsv.exceptions.CsvValidationException e) {
      System.out.println("handle CsvValidationException...");
    }
  }
  // end::importCSV[]
  
  
  // tag::importCSV_batch[]
  public void importCSV_batch() {
    // tag::omit[]
    System.out.println("importCSV_batch");
    // end::omit[]
    
    Flux<Map<String,String>> rows = Flux.generate(
      
      () -> new CSVReaderHeaderAware(
        new FileReader("modules/howtos/examples/import.csv")),
      
      (state, sink) -> {
        try {
          Map<String,String> row = state.readMap();
          if (row == null) { sink.complete(); }
          else { sink.next(row); }
          return state;
        }
        catch (Exception e) { throw new RuntimeException(e); }
      },
      state -> {
        try { state.close(); }
        catch (Exception e) { throw new RuntimeException(e); }
      }
    );

    Flux<MutationResult> results = 
      rows
      .map(row -> preprocess(row))
      .flatMap(doc -> reactiveCollection.upsert(doc.getId(), doc.getContent()))
      .doOnNext(System.out::println);

    results.blockLast(Duration.ofSeconds(60));
    // tag::omit[]
    System.out.println("DONE");
    // end::omit[]
  }
  // end::importCSV_batch[]

  
  // tag::importTSV[]
  public void importTSV() {
    CSVParser parser =
      new CSVParserBuilder()
      .withSeparator('\t')
      .withIgnoreQuotations(true)
      .build();
    
    try (CSVReaderHeaderAware tsv =
        new CSVReaderHeaderAwareBuilder(
          new FileReader("modules/howtos/examples/import.tsv"))
        .withCSVParser(parser)
        .build()) {

      Map<String, String> row;
      while ((row = tsv.readMap()) != null) {
        upsertRow(row);
      }
    }
    // ...
    // tag::omit[]
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    catch (com.opencsv.exceptions.CsvValidationException e) {
      System.out.println("handle CsvValidationException...");
    }
    // end::omit[]
  }
  // end::importTSV[]
  
  // tag::importTSV_batch[]
  public void importTSV_batch() {
    // tag::omit[]
    System.out.println("importTSV_batch");
    // end::omit[]

    Flux<Map<String,String>> rows = Flux.generate(
      
      () -> {
        CSVParser parser =
          new CSVParserBuilder()
          .withSeparator('\t')
          .withIgnoreQuotations(true)
          .build();
        return
          new CSVReaderHeaderAwareBuilder(
            new FileReader("modules/howtos/examples/import.tsv"))
          .withCSVParser(parser)
          .build();
      },
      
      // ...
      // tag::omit[]
      (state, sink) -> {
        try {
          Map<String,String> row = state.readMap();
          if (row == null) {
            sink.complete();
          }
          else {
            sink.next(row);
          }
          return state;
        }
        catch (Exception e) { throw new RuntimeException(e); }
      });

    Flux<MutationResult> results = 
      rows
      .map(row -> preprocess(row))
      .flatMap(doc -> reactiveCollection.upsert(doc.getId(), doc.getContent()))
      .doOnNext(System.out::println);

    results.blockLast(Duration.ofSeconds(60));

    System.out.println("DONE");
    // end::omit[]
  }
// end::importTSV_batch[]
  
  // tag::importJSON[]
  public void importJSON() {
    try {
      String content  = new String(
        Files.readAllBytes( // read whole document into memory
          Paths.get("modules/howtos/examples/import.json")),
        StandardCharsets.UTF_8);
      
      for (Object row: JsonArray.fromJson(content)) {
        JsonObject json = ((JsonObject) row);
        upsertRow(json.toMap());
      }
    }
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
  }
  // end::importJSON[]
  
  // tag::importJSON_batch[]
  public void importJSON_batch() {
    // tag::omit[]
    System.out.println("importJSON_batch");
    // end::omit[]

    try {
      String content  = new String(
        Files.readAllBytes( // read whole document into memory
          Paths.get("modules/howtos/examples/import.json")),
        StandardCharsets.UTF_8);
      
      Flux<MutationResult> results = 
        Flux.fromIterable(JsonArray.fromJson(content))
          .map(row -> ((JsonObject) row).toMap())
          .map(map -> preprocess(map))
          .flatMap(doc -> reactiveCollection.upsert(doc.getId(), doc.getContent()))
          .doOnNext(System.out::println);

      results.blockLast(Duration.ofSeconds(60));
    }
    // ...
    // tag::omit[]
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    System.out.println("DONE");
    // end::omit[]
  }
  // end::importJSON_batch[]
  
  // tag::importJSONL[]
  public void importJSONL() {
    try (BufferedReader br =
          new BufferedReader(
            new FileReader("modules/howtos/examples/import.jsonl"))) {
              
        String line;
        while ((line = br.readLine()) != null) {
          Map<String,Object> row =
            JsonObject.fromJson(line).toMap();
            
          upsertRow(row);
      }
    }
    // ...
    // tag::omit[]
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    // end::omit[]
  }
  // end::importJSONL[]
  
  // tag::importJSONL_batch[]
  public void importJSONL_batch() {
    // tag::omit[]
    System.out.println("importJSONL_batch");
    // end::omit[]

    Flux<String> lines = Flux.using(
      () -> Files.lines(Paths.get("modules/howtos/examples/import.jsonl")),
      Flux::fromStream,
      BaseStream::close);

    Flux<MutationResult> results =
      lines
          .map(line -> JsonObject.fromJson(line).toMap())
          .map(map -> preprocess(map))
          .flatMap(doc -> reactiveCollection.upsert(doc.getId(), doc.getContent()))
          .doOnNext(System.out::println);

    results.blockLast(Duration.ofSeconds(60));
    // tag::omit[]
    System.out.println("DONE");
    // end::omit[]
  }
  // end::importJSONL_batch[]

  // tag::JsonDocument[]
  class JsonDocument {
    private final String id;
    private final JsonObject content;

    public JsonDocument(String id, JsonObject content) {
      this.id = id;
      this.content = content;
    }

    public String getId() {
      return id;
    }

    public JsonObject getContent() {
      return content;
    }

    @Override
    public String toString() {
      return "JsonDocument{id='" + id + "', content=" + content + "}";
    }
  }
  // end::JsonDocument[]
}
