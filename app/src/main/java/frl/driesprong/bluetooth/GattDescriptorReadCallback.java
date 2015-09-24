package frl.driesprong.bluetooth;

public interface GattDescriptorReadCallback {
    void call(byte[] value);
}
