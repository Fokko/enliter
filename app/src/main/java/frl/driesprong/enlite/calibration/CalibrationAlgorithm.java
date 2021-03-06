package frl.driesprong.enlite.calibration;

import java.util.List;

public interface CalibrationAlgorithm {
    public double approximateGlucoseLevel(double sensorMeasurement, List<CalibrationPair> calibrationPoints);
}
