import java.time.Duration;

import com.couchbase.client.core.cnc.RequestTracer;
import com.couchbase.client.core.env.CoreEnvironment;
import com.couchbase.client.core.env.ThresholdLoggingTracerConfig;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.tracing.opentelemetry.OpenTelemetryRequestTracer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

public class Tracing {

  public static void main(String... args) throws Exception {
    {
      {
        // tag::tracing-configure[]
        ThresholdLoggingTracerConfig.Builder config = ThresholdLoggingTracerConfig.builder()
            .emitInterval(Duration.ofMinutes(1)).kvThreshold(Duration.ofSeconds(2));

        CoreEnvironment environment = CoreEnvironment.builder().thresholdLoggingTracerConfig(config).build();
        // end::tracing-configure[]
      }
    }
  }

  public static void opentelemetryDirect() {
    String hostname = "";
    String username = "";
    String password = "";

    // tag::otel-direct[]
    // Set the OpenTelemetry SDK's SdkTracerProvider
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .setResource(Resource.getDefault()
                    .merge(Resource.builder()
                            // An OpenTelemetry service name generally reflects the name of your microservice,
                            // e.g. "shopping-cart-service".
                            .put("service.name", "YOUR_SERVICE_NAME_HERE")
                            .build()))
            // The BatchSpanProcessor will efficiently batch traces and periodically export them.
            // This exporter exports traces on the OTLP protocol over GRPC to localhost:4317.
            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder()
                    .setEndpoint("http://localhost:4317")
                    .build()).build())
            // Export every trace: this may be too heavy for production.
            // An alternative is `.setSampler(Sampler.traceIdRatioBased(0.01))`
            .setSampler(Sampler.alwaysOn())
            .build();

    // Set the OpenTelemetry SDK's OpenTelemetry
    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .buildAndRegisterGlobal();

    Cluster cluster = Cluster.connect(hostname, ClusterOptions.clusterOptions(username, password)
            .environment(env -> {
              // Provide the OpenTelemetry object to the Couchbase SDK
              env.requestTracer(OpenTelemetryRequestTracer.wrap(openTelemetry));
            }));
    // end::otel-direct[]
  }
}
