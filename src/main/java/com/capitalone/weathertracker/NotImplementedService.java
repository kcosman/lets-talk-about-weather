package com.capitalone.weathertracker;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

import com.capitalone.weathertracker.measurements.*;
import com.capitalone.weathertracker.statistics.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// TODO: Delete this service and implement the service interfaces.
@Service
class NotImplementedService implements MeasurementQueryService, MeasurementStore, MeasurementAggregator {

  private SortedMap<ZonedDateTime,Measurement> zonedDateTimeMeasurementMap = new ConcurrentSkipListMap<ZonedDateTime, Measurement>();

  @Override
  public void add(Measurement measurement) {
    ZonedDateTime timestamp = measurement.getTimestamp();
    if (timestamp == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    measurement.getMetrics().values().forEach(value -> {
              if (value == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            });
    zonedDateTimeMeasurementMap.put(timestamp, measurement);
  }

  @Override
  public Measurement fetch(ZonedDateTime timestamp) {
    return zonedDateTimeMeasurementMap.get(timestamp);
  }

  @Override
  public List<Measurement> queryDateRange(ZonedDateTime from, ZonedDateTime to) {
    List<Measurement> measurementList = new ArrayList<>(zonedDateTimeMeasurementMap.subMap(from.minusNanos(1), to.minusNanos(1)).values());
//    if (!measurementList.isEmpty())
//        measurementList = measurementList.subList(1, measurementList.size());
    return measurementList;
  }

  @Override
  public List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats) {
    final List<AggregateResult> aggregateResultList = new ArrayList<>();
    if (measurements ==null || metrics == null || stats == null) return aggregateResultList;
    if (measurements.size() == 1) {
        metrics.forEach(metric -> {
            aggregateResultList.add(new AggregateResult(metric, Statistic.MIN, measurements.get(0).getMetric(metric)));
            aggregateResultList.add(new AggregateResult(metric, Statistic.MAX, measurements.get(0).getMetric(metric)));
            aggregateResultList.add(new AggregateResult(metric, Statistic.AVERAGE, measurements.get(0).getMetric(metric)));
        });
    }
    else {
        metrics.forEach(metric -> {
            double minMetric = measurements.stream()
                    .min(Comparator.comparing(measurement -> measurement.getMetric(metric))).get().getMetric(metric);
            aggregateResultList.add(new AggregateResult(metric, Statistic.MIN, minMetric));
            double maxMetric = measurements.stream()
                    .max(Comparator.comparing(measurement -> measurement.getMetric(metric))).get().getMetric(metric);
            aggregateResultList.add(new AggregateResult(metric, Statistic.MAX, maxMetric));
            double avgMetric = measurements.stream().mapToDouble(m -> m.getMetric(metric)).average().orElse(Double.NaN);
            aggregateResultList.add(new AggregateResult(metric, Statistic.AVERAGE, avgMetric));
        });
    }
    return aggregateResultList;
  }
}
