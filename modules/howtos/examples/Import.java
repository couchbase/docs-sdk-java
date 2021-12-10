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


public class Import {

  static String connectionString = "localhost";
  static String username = "Administrator";
  static String password = "password";
  static String bucketName = "travel-sample";

  private Collection collection;
  
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
  }
  
  public static void main(String... args) {
    Import importer = new Import();
    
    importer.importCSV();
    importer.importTSV();
    importer.importJSON();
    importer.importJSONL();
  }
  
  // NOTE: non-generic Map, because the readers produce slightly different outputs:
  //   CSV: Map<String,String>
  //   JSON: Map<String,Object>
  // and as generics annotations are lost at runtime we can't have
  // a single overloaded method to deal with those.
  //
  // In real code, you might choose to cast from Object to Map<String,Object>
  // or define multiple methods, but those confuse the example a little.
  
  // tag::upsertDocument[]
  public void upsertRow(Map row) {
    
    // define the KEY
    String key = row.get("type") + "_" + row.get("id");
    
    // do any additional processing
    row.put("importer", "Java SDK");
    
    // upsert the document
    collection.upsert(key, row);
    
    // any required logging
    System.out.println(key);
    System.out.println(row);
  }
  // end::upsertDocument[]

  
  // tag::importCSV[]
  public void importCSV() {
    try {
      CSVReaderHeaderAware csv = new CSVReaderHeaderAware(
        new FileReader("howtos/examples/import.csv"));
      
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
  
  // tag::importTSV[]
  public void importTSV() {
    CSVParser parser =
      new CSVParserBuilder()
      .withSeparator('\t')
      .withIgnoreQuotations(true)
      .build();
      
    
    try {
      CSVReaderHeaderAware tsv =
        new CSVReaderHeaderAwareBuilder(
          new FileReader("howtos/examples/import.tsv"))
        .withCSVParser(parser)
        .build();

      Map<String, String> row;
      while ((row = tsv.readMap()) != null) {
        upsertRow(row);
      }
    }
    // ...
    // tag::repeated[]
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    catch (com.opencsv.exceptions.CsvValidationException e) {
      System.out.println("handle CsvValidationException...");
    }
    // end::repeated[]
  }
  // end::importTSV[]
  
  // tag::importJSON[]
  public void importJSON() {
    try {
      String content  = new String(
        Files.readAllBytes(
          Paths.get("howtos/examples/import.json")),
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
  
  // tag::importJSONL[]
  public void importJSONL() {
    try {
        BufferedReader br =
          new BufferedReader(
            new FileReader("howtos/examples/import.jsonl"));
        String line;
        while ((line = br.readLine()) != null) {
          Map<String,Object> row =
            JsonObject.fromJson(line).toMap();
            
          upsertRow(row);
      }
    }
    // ...
    // tag::repeated[]
    catch (java.io.FileNotFoundException e) {
      System.out.println("handle FileNotFoundException...");
    }
    catch (java.io.IOException e) {
      System.out.println("handle IOException...");
    }
    // end::repeated[]
  }
  // tag::importJSONL[]
}
