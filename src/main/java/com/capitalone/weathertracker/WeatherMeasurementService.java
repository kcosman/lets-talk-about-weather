package com.capitalone.weathertracker;

import com.capitalone.weathertracker.measurements.Measurement;
import com.capitalone.weathertracker.measurements.MeasurementQueryService;
import com.capitalone.weathertracker.measurements.MeasurementStore;
import com.capitalone.weathertracker.statistics.AggregateResult;
import com.capitalone.weathertracker.statistics.MeasurementAggregator;
import com.capitalone.weathertracker.statistics.Statistic;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Service
class WeatherMeasurementService implements MeasurementQueryService, MeasurementStore, MeasurementAggregator {

    private SortedMap<Instant, Measurement> zonedDateTimeMeasurementMap = new ConcurrentSkipListMap<>();

    @Override
    public void add(Measurement measurement) {
        ZonedDateTime timestamp = measurement.getTimestamp();
        if (timestamp == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        measurement.getMetrics().values().forEach(value -> {
            try {
                Double.valueOf(value);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        });

        zonedDateTimeMeasurementMap.put(timestamp.toInstant(), measurement);
    }

    @Override
    public Measurement fetch(ZonedDateTime timestamp) {
        return zonedDateTimeMeasurementMap.get(timestamp.toInstant());
    }

    @Override
    public void delete(ZonedDateTime timestamp) {
        zonedDateTimeMeasurementMap.remove(timestamp.toInstant());
    }

    @Override
    public List<Measurement> queryDateRange(ZonedDateTime from, ZonedDateTime to) {
        if (to.isBefore(from)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        List<Measurement> measurementList = new ArrayList<>(zonedDateTimeMeasurementMap.subMap(
                from.toInstant().minus(1, ChronoUnit.NANOS),
                to.toInstant().minus(1, ChronoUnit.NANOS)).values());
        return measurementList;
    }

    @Override
    public List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats) {
        final List<AggregateResult> aggregateResultList = new ArrayList<>();
        if (measurements == null || metrics == null || stats == null) return aggregateResultList;
        if (measurements.size() == 1) {
            metrics.forEach(metric -> {
                aggregateResultList.add(new AggregateResult(metric, Statistic.MIN, measurements.get(0).getMetric(metric)));
                aggregateResultList.add(new AggregateResult(metric, Statistic.MAX, measurements.get(0).getMetric(metric)));
                aggregateResultList.add(new AggregateResult(metric, Statistic.AVERAGE, measurements.get(0).getMetric(metric)));
            });
        } else {
            metrics.forEach(metric -> {

                measurements.stream()
                        .filter(measurement -> measurement.getMetric(metric) != null)
                        .min(Comparator.comparing(measurement ->
                                measurement.getMetric(metric)))
                        .ifPresent(min ->
                                aggregateResultList.add(new AggregateResult(metric,
                                        Statistic.MIN,
                                        min.getMetric(metric))));

                measurements.stream()
                        .filter(measurement -> measurement.getMetric(metric) != null)
                        .max(Comparator.comparing(measurement ->
                                measurement.getMetric(metric)))
                        .ifPresent(max ->
                                aggregateResultList.add(new AggregateResult(metric,
                                        Statistic.MAX,
                                        max.getMetric(metric))));

                List<BigDecimal> bigDecimals = measurements.stream()
                        .filter(measurement -> measurement.getMetric(metric) != null)
                        .flatMapToDouble(measurement -> DoubleStream.of(measurement.getMetric(metric)))
                        .boxed()
                        .map(BigDecimal::valueOf)
                        .collect(Collectors.toList());
                if (!bigDecimals.isEmpty()) {
                    aggregateResultList.add(new AggregateResult(metric,
                            Statistic.AVERAGE,
                            average(bigDecimals, RoundingMode.HALF_UP).doubleValue()));
                }
            });
        }
        return aggregateResultList;
    }

    private BigDecimal average(List<BigDecimal> bigDecimals, RoundingMode roundingMode) {
        if (bigDecimals.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = bigDecimals.stream()
                .map(Objects::requireNonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(bigDecimals.size()), roundingMode);
    }
}
