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

import com.couchbase.client.core.cnc.Event;
import com.couchbase.client.core.env.LoggerConfig;
import com.couchbase.client.core.logging.LogRedaction;
import com.couchbase.client.core.logging.RedactionLevel;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollectingInformationAndLogging {

  String connectionString="localhost";
  String username="Administrator";
  String password="password";
  String bucketName="travel-sample";

  Cluster cluster;
  Bucket bucket;
  Scope scope;
  Collection collection;

  private void init(){
    // tag::connection_1[];
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    cluster = Cluster.connect(connectionString,      ClusterOptions.clusterOptions(username, password).environment(environment));
    bucket = cluster.bucket(bucketName);
    scope = bucket.defaultScope();
    collection = bucket.defaultCollection();
    // end::connection_1[]
  }
  public void collecting_information_and_logging_1() throws Exception { // file: howtos/pages/collecting-information-and-logging.adoc line: 114
    // tag::collecting_information_and_logging_1[]
    Logger logger = Logger.getLogger("com.couchbase.client");
    logger.setLevel(Level.FINE);
    for(Handler h : logger.getParent().getHandlers()) {
    	if(h instanceof ConsoleHandler){
        	h.setLevel(Level.FINE);
    	}
    }
    // end::collecting_information_and_logging_1[]
  }

  public void collecting_information_and_logging_2() throws Exception { // file: howtos/pages/collecting-information-and-logging.adoc line: 131
    // tag::collecting_information_and_logging_2[]
    ClusterEnvironment environment = ClusterEnvironment
      .builder()
      .loggerConfig(LoggerConfig
        .fallbackToConsole(true)
        .disableSlf4J(true)
      )
      .build();
    // end::collecting_information_and_logging_2[]
  }

  public void collecting_information_and_logging_3() throws Exception { // file: howtos/pages/collecting-information-and-logging.adoc line: 163
    // tag::collecting_information_and_logging_3[]
    ClusterEnvironment environment = ClusterEnvironment.builder().build();

    environment.eventBus().subscribe(event -> {
      // handle events as they arrive
      if (event.severity() == Event.Severity.INFO || event.severity() == Event.Severity.WARN) {
        System.out.println(event);
      }
    });

    Cluster cluster = Cluster.connect(
        connectionString,
        ClusterOptions.clusterOptions(username, password).environment(environment)
    );

    Bucket bucket = cluster.bucket(bucketName);
    // end::collecting_information_and_logging_3[]
  }

  public void collecting_information_and_logging_4() throws Exception { // file: howtos/pages/collecting-information-and-logging.adoc line: 206
    // tag::collecting_information_and_logging_4[]
    LogRedaction.setRedactionLevel(RedactionLevel.FULL);
    // end::collecting_information_and_logging_4[]
  }

  public static void main(String[] args) throws Exception{
    CollectingInformationAndLogging obj = new CollectingInformationAndLogging();
    obj.init();
    obj.collecting_information_and_logging_1();
    obj.collecting_information_and_logging_2();
    obj.collecting_information_and_logging_3();
    obj.collecting_information_and_logging_4();
    System.out.println("Done.");
  }
}
