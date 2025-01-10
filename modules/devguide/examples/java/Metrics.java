import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.metrics.opentelemetry.OpenTelemetryMeter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.*;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Duration;
import java.util.List;

public class Metrics {
  public static void main(String... args) throws Exception {

    {
      // tag::metrics-enable-custom[]
      ClusterEnvironment environment = ClusterEnvironment.builder()
              .loggingMeterConfig(config -> config.enabled(true).emitInterval(Duration.ofSeconds(30)))
              .build();
      // end::metrics-enable-custom[]
    }

    {
      String hostname = "localhost";
      String username = "Administrator";
      String password = "password";

      // tag::metrics-otel-prometheus[]
      // Setup an exporter.
      // This exporter exports traces on the OTLP protocol over GRPC to localhost:4317.
      MetricExporter exporter = OtlpGrpcMetricExporter.builder()
              .setCompression("gzip")
              .setEndpoint("http://localhost:4317")
              .build();

      // Create the OpenTelemetry SDK's SdkMeterProvider.
      SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
              .setResource(Resource.getDefault()
                      .merge(Resource.builder()
                              // An OpenTelemetry service name generally reflects the name of your microservice,
                              // e.g. "shopping-cart-service".
                              .put("service.name", "YOUR_SERVICE_NAME_HERE")
                              .build()))
              // Operation durations are in nanoseconds, which are too large for the default OpenTelemetry histogram buckets.
              .registerView(InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
                      View.builder().setAggregation(Aggregation.explicitBucketHistogram(List.of(
                              100000.0,
                              250000.0,
                              500000.0,
                              1000000.0,
                              10000000.0,
                              100000000.0,
                              1000000000.0,
                              10000000000.0))).build())
              .registerMetricReader(PeriodicMetricReader.builder(exporter).setInterval(Duration.ofSeconds(1)).build())
              .build();

      // Create the OpenTelemetry SDK's OpenTelemetry object.
      OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
              .setMeterProvider(sdkMeterProvider)
              .buildAndRegisterGlobal();

      // Provide the OpenTelemetry object as part of the Cluster configuration.
      Cluster cluster = Cluster.connect(hostname, ClusterOptions.clusterOptions(username, password)
              .environment(env -> env.meter(OpenTelemetryMeter.wrap(openTelemetry))));
      // end::metrics-otel-prometheus[]

      // A basic test to check the metrics are exporting.
      cluster.bucket("default").defaultCollection().upsert("doc", JsonObject.create());
    }
  }
}
