package frl.driesprong.decoding.packages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import frl.driesprong.bluetooth.CRC;

public class SensorReading extends MedtronicReading {
    public static final double SENSOR_CONVERSION_CONSTANT_VALUE = 160.72;
    public static final double SENSOR_CONVERSION_CONSTANT_VALUE2 = 5.8e-4;
    public static final double SENSOR_CONVERSION_CONSTANT_VALUE3 = 6.25e-6;
    public static final double SENSOR_CONVERSION_CONSTANT_VALUE4 = 1.5e-6;
    public static final int SENSOR_CONVERSION_CONSTANT_VALUE5 = 65536;

    private static final String TAG = "MedtronicMensor";

    private static final int NUMBER_OF_MEASUREMENTS = 8;

    private byte adjustmentValue = 0;
    private byte batteryLevel = 0;
    private byte sequence = 0;
    private final ArrayList<SensorMeasurement> isigData;

    // SO MANY MAGIC NUMBER LOLZ
    public SensorReading(final byte[] readData) throws ParseException {
        super(readData);

        this.isigData = new ArrayList<>(NUMBER_OF_MEASUREMENTS);

        if (readData.length != packageLength()) {
            throw new InvalidLengthException("Invalid message length");
        }

        // @TODO 5-7 Enlite ID, check this
        /*
        byte[] serial = convertSerialToBytes(2541711);

        if (serial[0] != readData[4] ||
                serial[1] != readData[5] ||
                serial[2] != readData[6]) {
            throw new InvalidSerialException("Invalid message serial");
        }*/

        byte[] crcComputed = CRC.computeCRC16(readData, 2, 34);
        if (crcComputed[0] != readData[readData.length - 2]
                || crcComputed[1] != readData[readData.length - 1]) {
            throw new InvalidCRCException("Invalid message CRC");
        }

        this.adjustmentValue = readData[9];
        this.sequence = readData[10];
        this.batteryLevel = readData[18];

        int seq = 0;
        // bytes 11 - 14
        for (int b = 11; b < 11 + 4; b += 2) {
            int measurement = ByteBuffer.wrap(new byte[]{0x00, 0x00, readData[b], readData[b + 1]}).getInt();
            double isig = calculateISIG((double) measurement, (double) this.adjustmentValue);

            isigData.add(new SensorMeasurement(getDate(EXPIRATION_FIVE_MINUTES * seq++), isig));
        }

        // bytes 19 - 30
        for (int b = 19; b < 19 + 12; b += 2) {
            int measurement = ByteBuffer.wrap(new byte[]{0x00, 0x00, readData[b], readData[b + 1]}).getInt();
            double isig = calculateISIG((double) measurement, (double) this.adjustmentValue);

            isigData.add(new SensorMeasurement(getDate(EXPIRATION_FIVE_MINUTES * seq++), isig));
        }
    }

    public Date getDate(int secondsAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -secondsAgo);

        return cal.getTime();
    }

    public byte getBatteryLevel() {
        return this.batteryLevel;
    }

    public short getAdjustmentValue() {
        return this.adjustmentValue;
    }

    public byte getSequenceNumber() {
        return this.sequence;
    }

    public List<SensorMeasurement> getIsigMeasurements() {
        return this.isigData;
    }

    private byte[] convertSerialToBytes(int i) {
        byte[] result = new byte[3];

        result[0] = (byte) (i >> 16);
        result[1] = (byte) (i >> 8);
        result[2] = (byte) i;

        return result;
    }

    public double calculateISIG(double value, double adjustment) {
        double isig = (float) value
                / (SENSOR_CONVERSION_CONSTANT_VALUE - ((float) value * SENSOR_CONVERSION_CONSTANT_VALUE2));
        isig += (adjustment * value * (SENSOR_CONVERSION_CONSTANT_VALUE3 + (SENSOR_CONVERSION_CONSTANT_VALUE4
                * value / SENSOR_CONVERSION_CONSTANT_VALUE5)));
        return isig;
    }

    @Override
    public String toString() {
        return "SensorReading Package, last reading: " + isigData.get(0);
    }

    @Override
    protected int packageLength() {
        return 36;
    }
}
