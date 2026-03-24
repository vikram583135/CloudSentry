package com.devops.platform.metrics.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Service for custom application metrics using Micrometer.
 */
@Service
@Slf4j
public class CustomMetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gaugeValues = new ConcurrentHashMap<>();

    public CustomMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        // Initialize counters
        counters.put("metrics.ingested", Counter.builder("metrics.ingested.total")
                .description("Total number of metrics ingested")
                .register(meterRegistry));

        counters.put("metrics.processed", Counter.builder("metrics.processed.total")
                .description("Total number of metrics processed")
                .register(meterRegistry));

        counters.put("metrics.errors", Counter.builder("metrics.errors.total")
                .description("Total number of metrics processing errors")
                .register(meterRegistry));

        counters.put("anomalies.detected", Counter.builder("anomalies.detected.total")
                .description("Total number of anomalies detected")
                .register(meterRegistry));

        counters.put("incidents.created", Counter.builder("incidents.created.total")
                .description("Total number of incidents created")
                .register(meterRegistry));

        // Initialize timers
        timers.put("metrics.ingestion", Timer.builder("metrics.ingestion.duration")
                .description("Time taken to ingest metrics")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));

        timers.put("metrics.processing", Timer.builder("metrics.processing.duration")
                .description("Time taken to process metrics")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));

        // Initialize gauge values
        gaugeValues.put("kafka.lag", new AtomicLong(0));
        gaugeValues.put("active.applications", new AtomicLong(0));
        gaugeValues.put("open.incidents", new AtomicLong(0));

        // Register gauges
        Gauge.builder("kafka.consumer.lag", gaugeValues.get("kafka.lag"), AtomicLong::get)
                .description("Kafka consumer lag")
                .register(meterRegistry);

        Gauge.builder("applications.active.count", gaugeValues.get("active.applications"), AtomicLong::get)
                .description("Number of active applications")
                .register(meterRegistry);

        Gauge.builder("incidents.open.count", gaugeValues.get("open.incidents"), AtomicLong::get)
                .description("Number of open incidents")
                .register(meterRegistry);
    }

    /**
     * Increments a counter by 1.
     */
    public void incrementCounter(String name) {
        Counter counter = counters.get(name);
        if (counter != null) {
            counter.increment();
        }
    }

    /**
     * Increments a counter by a specific amount.
     */
    public void incrementCounter(String name, double amount) {
        Counter counter = counters.get(name);
        if (counter != null) {
            counter.increment(amount);
        }
    }

    /**
     * Records the time for an operation.
     */
    public void recordTime(String name, long duration, TimeUnit unit) {
        Timer timer = timers.get(name);
        if (timer != null) {
            timer.record(duration, unit);
        }
    }

    /**
     * Records the time for an operation using a Supplier.
     */
    public <T> T recordTime(String name, Supplier<T> supplier) {
        Timer timer = timers.get(name);
        if (timer != null) {
            return timer.record(supplier);
        }
        return supplier.get();
    }

    /**
     * Sets a gauge value.
     */
    public void setGauge(String name, long value) {
        AtomicLong gauge = gaugeValues.get(name);
        if (gauge != null) {
            gauge.set(value);
        }
    }

    /**
     * Increments a gauge value.
     */
    public void incrementGauge(String name) {
        AtomicLong gauge = gaugeValues.get(name);
        if (gauge != null) {
            gauge.incrementAndGet();
        }
    }

    /**
     * Decrements a gauge value.
     */
    public void decrementGauge(String name) {
        AtomicLong gauge = gaugeValues.get(name);
        if (gauge != null) {
            gauge.decrementAndGet();
        }
    }

    /**
     * Creates a custom counter if it doesn't exist.
     */
    public Counter getOrCreateCounter(String name, String description, String... tags) {
        return counters.computeIfAbsent(name, n -> Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry));
    }

    /**
     * Creates a custom timer if it doesn't exist.
     */
    public Timer getOrCreateTimer(String name, String description, String... tags) {
        return timers.computeIfAbsent(name, n -> Timer.builder(name)
                .description(description)
                .tags(tags)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));
    }
}
