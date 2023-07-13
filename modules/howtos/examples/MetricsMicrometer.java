import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.metrics.micrometer.MicrometerMeter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.HTTPServer;

import java.net.InetSocketAddress;

public class MetricsMicrometer {
    public static void main(String... args) throws Exception {

        String hostname = "localhost";
        String username = "Administrator";
        String password = "password";

        // tag::metrics-micrometer-prometheus[]
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Micrometer won't create a histogram by default, configure that.
        prometheusRegistry.config().meterFilter(
                new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                        if (id.getType() == Meter.Type.DISTRIBUTION_SUMMARY) {
                            return DistributionStatisticConfig.builder()
                                    .percentilesHistogram(true)
                                    .build()
                                    .merge(config);
                        }
                        return config;
                    }
                });

        // Setup an HTTP server running on port 10000 that Prometheus can scrape for Micrometer metrics.
        new HTTPServer(new InetSocketAddress(10000), prometheusRegistry.getPrometheusRegistry(), true);

        // Provide the Micrometer registry as part of the Cluster configuration.
        Cluster cluster = Cluster.connect(hostname, ClusterOptions.clusterOptions(username, password)
                .environment(env -> env.meter(MicrometerMeter.wrap(prometheusRegistry))));
        // end::metrics-micrometer-prometheus[]

        while (true) {
            try {
                // A basic test to check the metrics are exporting.
                cluster.bucket("default").defaultCollection().upsert("doc", JsonObject.create());
            } catch (TimeoutException err) {
            }
        }
    }
}
