package com.capitalone.weathertracker.measurements;

import java.time.ZonedDateTime;

public interface MeasurementStore {
  void add(Measurement measurement);

  void delete(ZonedDateTime timestamp);

  Measurement fetch(ZonedDateTime timestamp);
}
