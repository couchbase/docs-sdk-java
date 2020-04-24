package com.couchbase.devguide;

import com.couchbase.client.core.diagnostics.*;
import com.couchbase.client.core.endpoint.EndpointState;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;


/**
 * Example Health Check with the Couchbase Java SDKa for the Couchbase Developer Guide.
 */
public class HealthCheck extends ConnectionBase {


//    public static void main(String... args) {

//        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
//
//        Bucket bucket = cluster.bucket("bucket-name");
//        Scope scope = bucket.scope("scope-name");
//        Collection collection = scope.collection("collection-name");
//
//        JsonObject json = JsonObject.create()
//                .put("foo", "bar")
//                .put("baz", "qux");
//
//
//// #tag::apis[]
//        AsyncCollection asyncCollection = collection.async();
//        ReactiveCollection reactiveCollection = collection.reactive();
//// #end::apis[]
//
//        JsonObject content = JsonObject.create().put("foo", "bar");
////        MutationResult result = collection.upsert("document-key", content);
//
//// #tag::apis[]
//        PingResult ping = bucket.ping();
//// #end::apis[]
    @Override
    protected void doWork() {

        bucket.waitUntilReady(Duration.ofSeconds(5));

        JsonObject content = JsonObject.create()
        .put("foo", "bar")
        .put("baz", "qux");

        MutationResult result = collection.upsert("document-key", content);

// #tag::ping[]
        // Ping a specified bucket to look at the state of all associated endpoints
        PingResult pingResult = bucket.ping();
        // Look at the KV endpoints and warn if their state is not OK
        Map<ServiceType, List<EndpointPingReport>> pingEndpoints = pingResult.endpoints();
        List<EndpointPingReport> kvPingReports = pingEndpoints.get(ServiceType.KV);

        for (EndpointPingReport pingEndpoint : kvPingReports) {
            if (pingEndpoint.state() != PingState.OK) {
                LOGGER.warn(String.format("Node %s at remote %s is %s.", pingEndpoint.id(), pingEndpoint.remote(), pingEndpoint.state()));
            } else {
                LOGGER.info(String.format("Node %s at remote %s is OK.", pingEndpoint.id(), pingEndpoint.remote()));
            }
        }
// #end::ping[]

// #tag::diagnostics[]
        // Get all diagnostics associated with a given cluster, passively
        DiagnosticsResult diagnosticsResult = cluster.diagnostics();
        Map<ServiceType, List<EndpointDiagnostics>> diagEndpoints = diagnosticsResult.endpoints();
        // Look at the KV connections, warn if not connected
        List<EndpointDiagnostics> kvDiagReports = diagEndpoints.get(ServiceType.KV);

        for (EndpointDiagnostics diagEndpoint : kvDiagReports) {
            // Identify the KV connection associated with the bucket we are using from the namespace
            if (diagEndpoint.namespace().isPresent() && diagEndpoint.namespace().get().contentEquals(bucketName)) {
                if (diagEndpoint.state() != EndpointState.CONNECTED) {
                    LOGGER.warn(String.format("Endpoint %s at remote %s is in state %s.", diagEndpoint.id(), diagEndpoint.remote(), diagEndpoint.state()));
                } else {
                    LOGGER.info(String.format("Endpoint %s at remote %s connected.", diagEndpoint.id().orElse("NO_ID"), diagEndpoint.remote()));
                }
            }
        }
// #end::diagnostics[]
    }

    public static void main(String[] args) {
        new HealthCheck().execute();
    }


}
