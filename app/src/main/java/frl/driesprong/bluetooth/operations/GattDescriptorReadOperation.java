package frl.driesprong.bluetooth.operations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

import frl.driesprong.bluetooth.GattAttributes;
import frl.driesprong.bluetooth.GattDescriptorReadCallback;

public class GattDescriptorReadOperation extends GattOperation {
    private static final String TAG = "GattDescriptorReadOperation";

    private final UUID mService;
    private final UUID mCharacteristic;
    private final UUID mDescriptor;
    private final GattDescriptorReadCallback mCallback;

    public GattDescriptorReadOperation(final UUID service,
                                       final UUID characteristic,
                                       final UUID descriptor,
                                       final GattDescriptorReadCallback callback) {
        super();
        mService = service;
        mCharacteristic = characteristic;
        mDescriptor = descriptor;
        mCallback = callback;
    }

    @Override
    public void execute(final BluetoothGatt gatt) {
        BluetoothGattDescriptor descriptor = gatt.getService(mService).getCharacteristic(mCharacteristic).getDescriptor(mDescriptor);
        gatt.readDescriptor(descriptor);
    }

    @Override
    public boolean hasAvailableCompletionCallback() {
        return true;
    }

    public void onRead(BluetoothGattDescriptor descriptor) {
        mCallback.call(descriptor.getValue());
    }

    @Override
    public String toString() {
        return "GattDescriptorReadOperation on service: " + GattAttributes.lookup(mService) + ", Char: " + GattAttributes.lookup(mCharacteristic);
    }
}
