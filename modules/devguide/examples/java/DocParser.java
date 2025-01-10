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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class DocParser {


  int fileNo = 0;
  BufferedWriter codeWriter;

  public static void main(String[] args) throws IOException {
    DocParser parser = new DocParser();
    for (String fileName : args) {
      try {
        parser.process(fileName);
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }

  final String preamble =
      "/*\n" +
          " * Copyright (c) 2020 Couchbase, Inc.\n" +
          " *\n" +
          " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
          " * you may not use this file except in compliance with the License.\n" +
          " * You may obtain a copy of the License at\n" +
          " *\n" +
          " *    http://www.apache.org/licenses/LICENSE-2.0\n" +
          " *\n" +
          " * Unless required by applicable law or agreed to in writing, software\n" +
          " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
          " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
          " * See the License for the specific language governing permissions and\n" +
          " * limitations under the License.\n" +
          " */\n" +
          "\n" +
          "import com.couchbase.client.core.cnc.Event;\n" +
          "import com.couchbase.client.core.diagnostics.PingResult;\n" +
          "import com.couchbase.client.core.encryption.CryptoManager;\n" +
          "import com.couchbase.client.core.endpoint.CircuitBreakerConfig;\n" +
          "import com.couchbase.client.core.env.Authenticator;\n" +
          "import com.couchbase.client.core.env.CompressionConfig;\n" +
          "import com.couchbase.client.core.env.IoConfig;\n" +
          "import com.couchbase.client.core.env.LoggerConfig;\n" +
          "import com.couchbase.client.core.env.NetworkResolution;\n" +
          "import com.couchbase.client.core.env.PasswordAuthenticator;\n" +
          "import com.couchbase.client.core.env.SecurityConfig;\n" +
          "import com.couchbase.client.core.env.SeedNode;\n" +
          "import com.couchbase.client.core.env.TimeoutConfig;\n" +
          "import com.couchbase.client.core.error.DocumentNotFoundException;\n" +
          "import com.couchbase.client.core.logging.LogRedaction;\n" +
          "import com.couchbase.client.core.logging.RedactionLevel;\n" +
          "import com.couchbase.client.core.msg.kv.DurabilityLevel;\n" +
          "import com.couchbase.client.core.retry.BestEffortRetryStrategy;\n" +
          "import com.couchbase.client.core.service.ServiceType;\n" +
          "import com.couchbase.client.encryption.AeadAes256CbcHmacSha512Provider;\n" +
          "import com.couchbase.client.encryption.DefaultCryptoManager;\n" +
          "import com.couchbase.client.encryption.KeyStoreKeyring;\n" +
          "import com.couchbase.client.encryption.Keyring;\n" +
          "import com.couchbase.client.java.Bucket;\n" +
          "import com.couchbase.client.java.Cluster;\n" +
          "import com.couchbase.client.java.ClusterOptions;\n" +
          "import com.couchbase.client.java.Collection;\n" +
          "import com.couchbase.client.java.Scope;\n" +
          "import com.couchbase.client.java.codec.JacksonJsonSerializer;\n" +
          "import com.couchbase.client.java.codec.TypeRef;\n" +
          "import com.couchbase.client.java.datastructures.CouchbaseArrayList;\n" +
          "import com.couchbase.client.java.datastructures.CouchbaseArraySet;\n" +
          "import com.couchbase.client.java.datastructures.CouchbaseMap;\n" +
          "import com.couchbase.client.java.datastructures.CouchbaseQueue;\n" +
          "import com.couchbase.client.java.diagnostics.PingOptions;\n" +
          "import com.couchbase.client.java.encryption.annotation.Encrypted;\n" +
          "import com.couchbase.client.java.encryption.databind.jackson.EncryptionModule;\n" +
          "import com.couchbase.client.java.env.ClusterEnvironment;\n" +
          "import com.couchbase.client.java.json.JsonArray;\n" +
          "import com.couchbase.client.java.json.JsonObject;\n" +
          "import com.couchbase.client.java.json.JsonObjectCrypto;\n" +
          "import com.couchbase.client.java.json.JsonValueModule;\n" +
          "import com.couchbase.client.java.kv.GetOptions;\n" +
          "import com.couchbase.client.java.kv.GetResult;\n" +
          "import com.couchbase.client.java.kv.InsertOptions;\n" +
          "import com.couchbase.client.java.kv.LookupInMacro;\n" +
          "import com.couchbase.client.java.kv.LookupInSpec;\n" +
          "import com.couchbase.client.java.kv.MutateInSpec;\n" +
          "import com.couchbase.client.java.kv.MutationResult;\n" +
          "import com.couchbase.client.java.kv.MutationState;\n" +
          "import com.couchbase.client.java.manager.bucket.BucketManager;\n" +
          "import com.couchbase.client.java.manager.bucket.BucketSettings;\n" +
          "import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;\n" +
          "import com.couchbase.client.java.manager.query.CreateQueryIndexOptions;\n" +
          "import com.couchbase.client.java.manager.query.QueryIndexManager;\n" +
          "import com.couchbase.client.java.query.QueryOptions;\n" +
          "import com.couchbase.client.java.query.QueryResult;\n" +
          "import com.couchbase.client.java.query.QueryScanConsistency;\n" +
          "\n" +
          "import java.io.FileInputStream;\n" +
          "import java.io.FileOutputStream;\n" +
          "import java.io.InputStream;\n" +
          "import java.io.OutputStream;\n" +
          "import java.security.KeyStore;\n" +
          "import java.security.SecureRandom;\n" +
          "import java.time.Duration;\n" +
          "import java.util.ArrayList;\n" +
          "import java.util.Arrays;\n" +
          "import java.util.Collections;\n" +
          "import java.util.EnumSet;\n" +
          "import java.util.HashSet;\n" +
          "import java.util.List;\n" +
          "import java.util.Map;\n" +
          "import java.util.Optional;\n" +
          "import java.util.Queue;\n" +
          "import java.util.Set;\n" +
          "import java.util.UUID;\n" +
          "import java.util.concurrent.CompletableFuture;\n" +
          "import java.util.concurrent.TimeUnit;\n" +
          "import java.util.logging.ConsoleHandler;\n" +
          "import java.util.logging.Handler;\n" +
          "import java.util.logging.Level;\n" +
          "import java.util.logging.Logger;\n" +
          "\n" +
          "import com.couchbase.client.java.search.SearchQuery;\n" +
          "import com.couchbase.client.java.search.result.SearchResult;\n" +
          "import com.couchbase.client.java.search.result.SearchRow;\n" +
          "import com.couchbase.client.java.view.ViewResult;\n" +
          "import com.couchbase.client.java.view.ViewRow;\n" +
          "import com.couchbase.transactions.error.TransactionFailed;\n" +
          "import com.couchbase.transactions.log.LogDefer;\n" +
          "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
          "import jdk.internal.org.objectweb.asm.commons.Remapper;\n" +
          "import jdk.nashorn.internal.runtime.logging.DebugLogger;\n" +
          "import org.springframework.beans.factory.annotation.Autowired;\n" +
          "import org.springframework.stereotype.Service;\n" +
          "\n" +
          "public class ${CLASSNAME} {\n" +
          "\n" +
          "  private static final String FLIGHTS_COLLECTION_NAME = \"flightsCollection\";\n" +
          "  private static final String USERS_COLLECTION_NAME = \"usersCollection\";\n" +
          "\n" +
          "  String connectionString=\"localhost\";\n" +
          "  String username=\"Administrator\";\n" +
          "  String password=\"password\";\n" +
          "  String bucketName=\"travel-sample\";\n" +

          "  String id = \"user::\" + UUID.randomUUID();\n" +
          "  DurabilityLevel expiry;\n" +
          "\n" +
          "  Cluster cluster;\n" +
          "  Bucket bucket;\n" +
          "  Scope scope;\n" +
          "  Collection collection;\n" +
          "\n" +
          "  BucketSettings bucketSettings;\n" +
          "\n" +
          "  CouchbaseMap map;\n" +
          "  CouchbaseArrayList arrayList;\n" +
          "  CouchbaseArraySet arraySet;\n" +
          "  CouchbaseQueue queue;\t" +
          "\n" +
          "  private void init(){\n" +
          "    ClusterEnvironment environment = ClusterEnvironment.builder().build();\n" +
          "    cluster = Cluster.connect(connectionString,\n" +
          "      ClusterOptions.clusterOptions(username, password).environment(environment));\n" +
          "    bucket = cluster.bucket(\"travel-sample\");\n" +
          "    scope = bucket.defaultScope();\n" +
          "    collection = bucket.defaultCollection();\n" +
          "  }\n";

  String postamble = "}\n";

  private BufferedWriter openCodeFileIfNecessary(String dirName, String classname) throws IOException {
    if (codeWriter == null) {
      File dir = new File(dirName);
      if (!dir.exists()) {
        dir.mkdir();
      }
      codeWriter = new BufferedWriter(new FileWriter(dirName + "/" + classname + ".java"));
      codeWriter.write(preamble.replace("${CLASSNAME}", classname));
    }
    return codeWriter;
  }

  private void closeCodeFile() throws IOException {
    if (codeWriter != null) {
      codeWriter.write(postamble);
      codeWriter.close();
      codeWriter = null;
    }
  }

  /*
   * For each file
   * 1) create a new *.adoc file containing the in-line [source,java] replaced with an include
   * 2) create a new *.java file containing a method for each [source,java]
   *
   */
  public void process(String fileName) throws IOException {
    fileNo++;
    int lineno = 0;
    int sourceSection = 0;
    Queue<String> adoc = new LinkedList<>();
    String dirName = fileName.substring(0, fileName.lastIndexOf("/"));
    String baseName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".adoc"));
    String javaName = baseName
        .replace("-", "_")
        .replace(".", "_");
    String javaFilename = dirName + "/../examples" + "/" + javaName + ".java";
    File javaFile = new File(javaFilename);
    if (javaFile.exists()) {
      System.out.println("java file already exists: " + javaFile.getPath());
      return;
    } else {
      System.out.println("creating java file " + javaFile.getPath());
    }
    try {
      BufferedReader input = new BufferedReader(new FileReader(fileName));
      Queue<String> methods = new LinkedList();
      boolean dontEncapsulate;
      String line;
      boolean continued = false;
      while ((line = input.readLine()) != null && ++lineno > 0) {
        if (!line.contains("[source,java]")) {
          adoc.add(line);
        } else if (line.contains("[source,java]")) {
          sourceSection++;
          adoc.add(line);
          if ((line = input.readLine()) != null && ++lineno > 0 && line.startsWith("----")) {
            adoc.add(line);
          } else {
            // that could have been the Title
            adoc.add(line);
            if ((line = input.readLine()) != null && ++lineno > 0 && line.startsWith("----")) {
              adoc.add(line);
            } else {
              throw new RuntimeException(fileName + ": source,java did not begin with ----  at line " + lineno);
            }
          }
          if ((line = input.readLine()) != null && ++lineno > 0 && !line.startsWith("include:")) {
            if (line.contains("// SDK 2")) {
              adoc.add(line);
              continue;
            }

            dontEncapsulate = (continued || line.startsWith("class") || line.startsWith("public ") || line.startsWith("import ") || line.startsWith("@Service"));

            Queue<String> methodLines = new LinkedList<>();
            if (!dontEncapsulate && !continued) {
              methodLines.add("  public void " + javaName + "_" + sourceSection + "() throws Exception { // file: " + fileName + " line: " + lineno);
            } else {
              methodLines.add("    // file: " + fileName + " line: " + lineno);
            }
            methodLines.add("    // tag::" + javaName + "_" + sourceSection + "[]");
            methodLines.add("    " + line); // first line

            // read in the method into

            while ((line = input.readLine()) != null && ++lineno > 0 && !line.startsWith("----")) { // ---- to ----
              if (line.endsWith("build()")) { // (1) build() missing semi-colon
                line = line + ";";
              }
              if (line.contains("...")) { // (2) remove ellipsis
                line = line.replace("...", "");
              }
              methodLines.add("    " + line); // remaining lines
              continued = line.contains("// continued"); // don't close-out this method.
            } // while content between ---- and ---- of [source,java]
            methodLines.add("    // end::" + javaName + "_" + sourceSection + "[]");
            if (!dontEncapsulate && !continued) { // close off the method
              methodLines.add("  }\n");
            }

            System.out.println(fileName + " " + (methodLines.size() - 4) + " " + sourceSection + " " + lineno);

            // write out the method

            if (methodLines.size() > 4) { // output method ( 4 = method, open tag, close tag, close parentheses )
              adoc.add("include::example$" + javaName + ".java[tag=" + javaName + "_" + sourceSection + ",indent=0]");
              adoc.add(line);
              methods.add(javaName + "_" + sourceSection); // this will also create a method call when there is no 'method'
              codeWriter = openCodeFileIfNecessary(dirName + "/../examples", javaName);
              while (methodLines.size() > 0) {
                line = methodLines.poll();
                codeWriter.write(line);
                if (methodLines.size() == 1 && !(line.endsWith(";") || line.endsWith("}"))) { // (3) last line missing semi-colon
                  codeWriter.write(";");
                }
                codeWriter.write("\n");
              }
            } // write out the method method
          } else { // this is an include
            System.out.println(fileName + " 0 " + sourceSection + " " + lineno + " " + line);
            adoc.add(line);
          }
        }// [source,java]
      }// while line != null

      input.close();
      // create a main() that calls all the methods

      if (methods.size() > 0) {
        codeWriter.write("  public static void main(String[] args) throws Exception{\n");
        codeWriter.write("    " + javaName + " obj = new " + javaName + "();\n");
        codeWriter.write("    obj.init();\n");

        while (methods.size() > 0) {
          codeWriter.write("    obj." + methods.poll() + "();\n");
        }
        codeWriter.write("  }\n");

        //long pid = ProcessHandle.current().pid();
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = processName.substring(0, processName.indexOf("@"));
        String archName = dirName + "/" + baseName + "." + pid;
        String saveName = dirName + "/" + baseName + ".sav";
        File saveFile = new File(saveName);
        File archFile = new File(archName);
        File inFile = new File(fileName);
        // make a .sav copy of the original adoc
        if (!saveFile.exists()) {
          if (!(inFile.renameTo(saveFile))) { // rename it to keep the timestamp etc.
            throw new IOException("could not rename " + saveName + " to " + archName);
          } else {
            Files.copy(saveFile.toPath(), inFile.toPath()); // copy it back so we can "archive" it.
          }
        }
        if (!(new File(fileName)).renameTo(archFile)) {
          throw new IOException("could not rename " + fileName + " to " + saveName);
        }
        BufferedWriter output = new BufferedWriter(new FileWriter(dirName + "/" + baseName + ".adoc"));
        while (adoc.size() > 0) {
          output.write(adoc.poll() + "\n");
        }
        output.close();

      }
      closeCodeFile();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
