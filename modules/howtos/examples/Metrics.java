import com.couchbase.client.core.env.AggregatingMeterConfig;
import com.couchbase.client.core.env.CoreEnvironment;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.metrics.opentelemetry.OpenTelemetryMeter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.prometheus.client.exporter.HTTPServer;

import java.time.Duration;

public class Metrics {
  public static void main(String... args) throws Exception {

    {
      // tag::metrics-enable[]
      CoreEnvironment environment = CoreEnvironment
        .builder()
        .aggregatingMeterConfig(AggregatingMeterConfig.enabled(true))
        .build();
      // end::metrics-enable[]
    }

    {
      // tag::metrics-enable-custom[]
      CoreEnvironment environment = CoreEnvironment
        .builder()
        .aggregatingMeterConfig(
          AggregatingMeterConfig
            .enabled(true)
            .emitInterval(Duration.ofSeconds(30))
        )
        .build();
      // end::metrics-enable-custom[]
    }

    {
      // tag::metrics-otel-prometheus[]
      // Build the OpenTelemetry Meter
      MeterSdkProvider meterSdkProvider = OpenTelemetrySdk.getGlobalMeterProvider();
      Meter meter = meterSdkProvider.get("OpenTelemetryMetricsSample");

      // Start the Prometheus HTTP Server
      HTTPServer server = server = new HTTPServer(19090);

      // Register the Prometheus Collector
      PrometheusCollector.builder()
        .setMetricProducer(meterSdkProvider.getMetricProducer())
        .buildAndRegister();
      // end::metrics-otel-prometheus[]

      // tag::metrics-otel-env[]
      ClusterEnvironment environment = ClusterEnvironment
        .builder()
        .meter(OpenTelemetryMeter.wrap(meter))
        .build();
      // end::metrics-otel-env[]
    }

  }
}
