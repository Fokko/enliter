package frl.driesprong.processing;

import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import frl.driesprong.decoding.packages.MedtronicReading;
import frl.driesprong.decoding.packages.MeterReading;
import frl.driesprong.decoding.packages.SensorMeasurement;
import frl.driesprong.decoding.packages.SensorReading;
import frl.driesprong.decoding.packages.SensorWarmupReading;
import frl.driesprong.enlite.calibration.CalibrationPair;
import frl.driesprong.enlite.calibration.LinearRegressionCalibration;
import frl.driesprong.enlite.calibration.OnePointCalibration;
import frl.driesprong.enlite.calibration.TwoPointCalibration;

public class MedtronicProcessor {
    private static final String TAG = "MedtronicProcessor";
    private final int KEEP_READINGS = 10;

    final List<CalibrationPair> knownCalibrations;
    final CircularFifoQueue<SensorMeasurement> lastSensorReadings;
    final CircularFifoQueue<MeterReading> lastGlucoseReadings;

    private static MedtronicProcessor instance;

    public static MedtronicProcessor getInstance() {
        if (instance == null) {
            synchronized (MedtronicProcessor.class) {
                if (instance == null) {
                    instance = new MedtronicProcessor();
                }
            }
        }
        return instance;
    }

    public MedtronicProcessor() {
        this.knownCalibrations = new LinkedList<>();
        this.lastSensorReadings = new CircularFifoQueue<>(KEEP_READINGS);
        this.lastGlucoseReadings = new CircularFifoQueue<>(KEEP_READINGS);
    }

    public void process(final MedtronicReading packet) {
        if (packet instanceof SensorReading) {
            SensorReading sensorReading = (SensorReading) packet;
            Log.d(TAG, "Found an Enlite package");

            for (SensorMeasurement measurement : sensorReading.getIsigMeasurements()) {
                if (!this.lastSensorReadings.contains(sensorReading)) {
                    Log.d(TAG, "Found a new measurements");
                    this.lastSensorReadings.add(measurement);

                    // Look if it possible to convert the SGV to a Glucose Level based on the calibration scheme
                    int calibrations = this.knownCalibrations.size();

                    double approximatedGlucoseLevel = -1;
                    if (calibrations == 1) {
                        approximatedGlucoseLevel = new OnePointCalibration().approximateGlucoseLevel(measurement.getIsig(), this.knownCalibrations);
                    } else if (calibrations == 2) {
                        approximatedGlucoseLevel = new TwoPointCalibration().approximateGlucoseLevel(measurement.getIsig(), this.knownCalibrations);
                    } else if (calibrations >= 3) {
                        approximatedGlucoseLevel = new LinearRegressionCalibration().approximateGlucoseLevel(measurement.getIsig(), this.knownCalibrations);
                    }

                }
            }

        } else if (packet instanceof SensorWarmupReading) {
            // If the sensor is warming up, we need new calibrations
            this.knownCalibrations.clear();
        } else if (packet instanceof MeterReading) {
            MeterReading glucoseReading = (MeterReading) packet;
            
        }
    }

    public void processVingerTest(MeterReading glucoseReading ) {

        // Check if it is new material
        if (!this.lastGlucoseReadings.contains(glucoseReading)) {
            this.lastGlucoseReadings.add(glucoseReading);

            // Look if the Meter value can be paired with a sensor value to create a calibration point
            long secondsDifference = Long.MAX_VALUE;
            double nearestSensorReading = 0;

            for (SensorMeasurement measurement : this.lastSensorReadings) {
                long diff = glucoseReading.createdDifference(measurement.getCreated());
                if (diff < secondsDifference) {
                    secondsDifference = diff;
                    nearestSensorReading = measurement.getIsig();
                }
            }

            // Check if they are close enough
            if (secondsDifference < MedtronicReading.EXPIRATION_FOUR_MINUTES) {
                this.knownCalibrations.add(new CalibrationPair(nearestSensorReading, glucoseReading.getMgdl()));
            }
        }
    }

    public Queue<MeterReading> getClucose() {
        return this.lastGlucoseReadings;
    }

    public List<CalibrationPair> getCalibrationPairs() {
        return this.knownCalibrations;
    }

}
