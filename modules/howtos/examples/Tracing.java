import java.time.Duration;

import com.couchbase.client.core.cnc.RequestTracer;
import com.couchbase.client.core.env.CoreEnvironment;
import com.couchbase.client.core.env.ThresholdRequestTracerConfig;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.tracing.opentelemetry.OpenTelemetryRequestTracer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class Tracing {

  public static void main(String... args) throws Exception {
    {
      {
        // tag::tracing-configure[]
        ThresholdRequestTracerConfig.Builder config = ThresholdRequestTracerConfig.builder()
            .emitInterval(Duration.ofMinutes(1)).kvThreshold(Duration.ofSeconds(2));

        CoreEnvironment environment = CoreEnvironment.builder().thresholdRequestTracerConfig(config).build();
        // end::tracing-configure[]
      }

      {
        // tag::otel-configure[]
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress("localhost", 14250).usePlaintext().build();

        // Export traces to Jaeger
        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setServiceName("otel-jaeger-example")
            .setChannel(jaegerChannel).setDeadlineMs(30000).build();

        // Set to process the spans by the Jaeger Exporter
        OpenTelemetrySdk.getGlobalTracerManagement()
            .addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter).build());
        // end::otel-configure[]

        // tag::otel-configure-setup[]
        // Wrap Tracer
        RequestTracer tracer = OpenTelemetryRequestTracer.wrap(OpenTelemetry.get());

        ClusterEnvironment environment = ClusterEnvironment.builder().requestTracer(tracer).build();
        // end::otel-configure-setup[]
      }
    }
  }
}
