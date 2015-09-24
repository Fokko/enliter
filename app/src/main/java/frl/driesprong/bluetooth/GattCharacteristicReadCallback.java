package frl.driesprong.bluetooth;

public interface GattCharacteristicReadCallback {
    void call(byte[] characteristic);
}
