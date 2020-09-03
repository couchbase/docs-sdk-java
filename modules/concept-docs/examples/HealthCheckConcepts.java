import com.couchbase.client.core.diagnostics.DiagnosticsResult;
import com.couchbase.client.core.diagnostics.EndpointDiagnostics;
import com.couchbase.client.core.diagnostics.EndpointPingReport;
import com.couchbase.client.core.diagnostics.PingResult;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.diagnostics.PingOptions;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.couchbase.client.java.diagnostics.PingOptions.pingOptions;

public class HealthCheckConcepts {

	public static void main(String... args) {
		Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
		Bucket bucket = cluster.bucket("bucket-name");
		Scope scope = bucket.scope("scope-name");
		Collection collection = scope.collection("collection-name");

		{
// #tag::ping-basic[]
			PingResult pingResult = cluster.ping();

			for (Map.Entry<ServiceType, List<EndpointPingReport>> service : pingResult.endpoints().entrySet()) {
				for (EndpointPingReport er : service.getValue()) {
					System.err.println(
							service.getKey() + ": " + er.remote() + " took " + er.latency()
					);
				}
			}
// #end::ping-basic[]
		}

		{
// #tag::ping-json-export[]
			PingResult pingResult = cluster.ping();

			System.out.println(pingResult.exportToJson());
// #end::ping-json-export[]
		}

		{
// #tag::ping-options[]
			PingResult pingResult = cluster.ping(pingOptions().serviceTypes(EnumSet.of(ServiceType.QUERY)));

			System.out.println(pingResult.exportToJson());
// #end::ping-options[]
		}

		{
// #tag::diagnostics-basic[]
			DiagnosticsResult diagnosticsResult = cluster.diagnostics();

			for (Map.Entry<ServiceType, List<EndpointDiagnostics>> service : diagnosticsResult.endpoints().entrySet()) {
				for (EndpointDiagnostics ed : service.getValue()) {
					System.err.println(
							service.getKey() + ": " + ed.remote() + " last activity  " + ed.lastActivity()
					);
				}
			}
// #end::diagnostics-basic[]
		}

		{
// #tag::diagnostics-options[]
			DiagnosticsResult diagnosticsResult = cluster.diagnostics();

			System.out.println(diagnosticsResult.exportToJson());
// #end::diagnostics-options[]
		}

		{
			// #tag::health_check_1[]
			PingResult pingResult = cluster.ping();
			System.out.println(pingResult.exportToJson());
			// #end::health_check_1[];
		}

		{
			// #tag::health_check_2[]
			cluster.ping(PingOptions.pingOptions().serviceTypes(EnumSet.of(ServiceType.QUERY)));
			// #end::health_check_2[];
		}

		{
			// #tag::health_check_3[]
			System.out.println(cluster.diagnostics().exportToJson());
			// #end::health_check_3[];
		}


	}

}
