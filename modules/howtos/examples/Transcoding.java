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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import com.couchbase.client.core.error.DecodingFailureException;
import com.couchbase.client.core.msg.kv.CodecFlags;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.codec.JsonSerializer;
import com.couchbase.client.java.codec.JsonTranscoder;
import com.couchbase.client.java.codec.RawBinaryTranscoder;
import com.couchbase.client.java.codec.RawJsonTranscoder;
import com.couchbase.client.java.codec.RawStringTranscoder;
import com.couchbase.client.java.codec.Transcoder;
import com.couchbase.client.java.codec.TypeRef;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.UpsertOptions;
import com.google.gson.Gson;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.msgpack.MessagePack;

// TODO: remove TypeRef overload when JCBC-1588 done
// tag::gson-serializer[]
class GsonSerializer implements JsonSerializer {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(Object input) {
        String json = gson.toJson(input);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(Class<T> target, byte[] input) {
        String str = new String(input, StandardCharsets.UTF_8);
        return gson.fromJson(str, target);
    }

    @Override
    public <T> T deserialize(TypeRef<T> target, byte[] input) {
        String str = new String(input, StandardCharsets.UTF_8);
        return gson.fromJson(str, target.type());
    }
}
// end::gson-serializer[]

// TODO: remove TypeRef overload when JCBC-1588 done
// tag::msgpack-serializer[]
class MsgPackSerializer implements JsonSerializer {
    private final MessagePack msgpack = new MessagePack();

    @Override
    public byte[] serialize(Object input) {
        try {
            return msgpack.write(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> target, byte[] input) {
        try {
            return msgpack.read(input, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(TypeRef<T> target, byte[] input) {
        throw new DecodingFailureException("Does not support decoding via TypeRef.");
    }
}
// end::msgpack-serializer[]

// tag::msgpack-transcoder[]
class MsgPackTranscoder implements Transcoder {
    private final MsgPackSerializer serializer = new MsgPackSerializer();

    @Override
    public EncodedValue encode(Object input) {
        byte[] serialized = serializer.serialize(input);
        return new EncodedValue(serialized, CodecFlags.BINARY_COMPAT_FLAGS);
    }

    @Override
    public <T> T decode(Class<T> target, byte[] input, int flags) {
        return serializer.deserialize(target, input);
    }
}
// end::msgpack-transcoder[]

class User {
    private final String name;
    private final int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return age == user.age && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}

public class Transcoding {
    private static Collection collection;

    @BeforeAll
    public static void BeforeAll() {
        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");

        Bucket bucket = cluster.bucket("default");
        bucket.waitUntilReady(Duration.ofSeconds(30));
        collection = bucket.defaultCollection();
    }

    @Test
    void gson() {
        // tag::gson-encode[]
        // User is a simple POJO
        User user = new User("John Smith", 27);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        collection.upsert("john-smith", json, UpsertOptions.upsertOptions().transcoder(RawJsonTranscoder.INSTANCE));
        // end::gson-encode[]

        // tag::gson-decode[]
        GetResult result = collection.get("john-smith", GetOptions.getOptions().transcoder(RawJsonTranscoder.INSTANCE));

        String returnedJson = result.contentAs(String.class);
        User returnedUser = gson.fromJson(returnedJson, User.class);
        // end::gson-decode[]

        assertEquals(json, returnedJson);
        assertEquals(user, returnedUser);
    }

    @Test
    void gsonCustom() {
        // tag::gson-custom-encode[]
        GsonSerializer serializer = new GsonSerializer();
        JsonTranscoder transcoder = JsonTranscoder.create(serializer);

        User user = new User("John Smith", 27);

        collection.upsert("john-smith", user, UpsertOptions.upsertOptions().transcoder(transcoder));
        // end::gson-custom-encode[]

        // tag::gson-custom-decode[]
        GetResult result = collection.get("john-smith", GetOptions.getOptions().transcoder(transcoder));

        User returnedUser = result.contentAs(User.class);

        assertEquals(user, returnedUser);
        // end::gson-custom-decode[]
    }

    @Test
    void gsonRegister() {
        // tag::gson-register-1[]
        GsonSerializer serializer = new GsonSerializer();

        ClusterEnvironment env = ClusterEnvironment.builder().jsonSerializer(serializer).build();
        // end::gson-register-1[]

        Cluster cluster = Cluster.connect("localhost",
                ClusterOptions.clusterOptions("Administrator", "password").environment(env));

        Collection coll = cluster.bucket("default").defaultCollection();

        // tag::gson-register-2[]
        User user = new User("John Smith", 27);

        coll.upsert("john-smith", user);

        GetResult result = coll.get("john-smith");

        User returnedUser = result.contentAs(User.class);
        // end::gson-register-2[]

        assertEquals(user, returnedUser);

        env.shutdown();
    }

    @Test
    void msgpackSimple() throws IOException {
        MessagePack msgpack = new MessagePack();

        String input = "hello world!";

        byte[] raw = msgpack.write(input);

        String output = msgpack.read(raw, String.class);
    }

    @Test
    void msgpack() {
        // tag::msgpack-encode[]
        MsgPackTranscoder transcoder = new MsgPackTranscoder();

        String input = "hello world!";

        collection.upsert("msgpack-doc", input, UpsertOptions.upsertOptions().transcoder(transcoder));

        GetResult result = collection.get("msgpack-doc", GetOptions.getOptions().transcoder(transcoder));

        String output = result.contentAs(String.class);
        // end::msgpack-encode[]

        assertEquals(input, output);
    }

    @Test
    void string() {
        String docId = "doc";
        // tag::string[]
        collection.upsert(docId, "hello world", UpsertOptions.upsertOptions().transcoder(RawStringTranscoder.INSTANCE));

        GetResult result = collection.get(docId, GetOptions.getOptions().transcoder(RawStringTranscoder.INSTANCE));

        String returned = result.contentAs(String.class);
        // end::string[]
        assertEquals(returned, "hello world");
    }

    @Test
    void binary() {
        String docId = "doc";
        // tag::binary[]
        String input = "hello world";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        collection.upsert(docId, bytes, UpsertOptions.upsertOptions().transcoder(RawBinaryTranscoder.INSTANCE));

        GetResult result = collection.get(docId, GetOptions.getOptions().transcoder(RawBinaryTranscoder.INSTANCE));

        byte[] returned = result.contentAs(byte[].class);
        // end::binary[]
        assertTrue(Arrays.equals(returned, bytes));
    }
}
