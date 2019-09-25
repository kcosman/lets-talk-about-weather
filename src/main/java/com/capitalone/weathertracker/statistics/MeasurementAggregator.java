package com.capitalone.weathertracker.statistics;

import com.capitalone.weathertracker.measurements.Measurement;

import java.util.List;

public interface MeasurementAggregator {
    List<AggregateResult> analyze(List<Measurement> measurements, List<String> metrics, List<Statistic> stats);
}
