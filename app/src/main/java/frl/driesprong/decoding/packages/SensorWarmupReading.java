package frl.driesprong.decoding.packages;

public class SensorWarmupReading extends MeterReading {
    public SensorWarmupReading(byte[] readData) throws ParseException {
        super(readData);
    }
}
