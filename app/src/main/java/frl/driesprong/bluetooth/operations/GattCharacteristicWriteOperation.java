package frl.driesprong.bluetooth.operations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

import frl.driesprong.HexDump;
import frl.driesprong.bluetooth.CRC;
import frl.driesprong.bluetooth.GattAttributes;
import frl.driesprong.bluetooth.RileyLinkUtil;

public class GattCharacteristicWriteOperation extends GattOperation {
    private static final String TAG = "GattCharacteristicWriteOperation";

    private final UUID mService;
    private final UUID mCharacteristic;
    private final byte[] mValue;

    public GattCharacteristicWriteOperation(final UUID service, final UUID characteristic, byte[] value, final boolean addCRC, final boolean transform) {
        super();
        mService = service;
        mCharacteristic = characteristic;

        if (addCRC) {
            value = CRC.appendCRC(value);
            Log.d(TAG, "CRC: " + HexDump.toHexString(value));
        }

        if (transform) {
            value = RileyLinkUtil.composeRFStream(value);
            Log.d(TAG, "Transformed: " + HexDump.toHexString(value));
        }

        mValue = value;
    }

    @Override
    public void execute(final BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = gatt.getService(mService).getCharacteristic(mCharacteristic);
        characteristic.setValue(mValue);
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    @Override
    public String toString() {
        return "GattCharacteristicWriteOperation on service: " + GattAttributes.lookup(mService) + ", Char: " + GattAttributes.lookup(mCharacteristic);
    }
}
