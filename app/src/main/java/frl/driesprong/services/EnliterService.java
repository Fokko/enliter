package frl.driesprong.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.UUID;

import frl.driesprong.Constants;
import frl.driesprong.Intents;
import frl.driesprong.bluetooth.BluetoothConnection;
import frl.driesprong.bluetooth.Commands;
import frl.driesprong.bluetooth.GattAttributes;
import frl.driesprong.bluetooth.GattCharacteristicReadCallback;
import frl.driesprong.bluetooth.operations.GattCharacteristicReadOperation;
import frl.driesprong.bluetooth.operations.GattCharacteristicWriteOperation;
import frl.driesprong.bluetooth.operations.GattInitializeBluetooth;

public class EnliterService extends IntentService {
    private static final String TAG = "EnliterService";
    private static Context context;

    public EnliterService() {
        super(TAG);
        context = this;

    }

    @Override
    public void onCreate() {
        BluetoothConnection conn = BluetoothConnection.getInstance();

        conn.queue(new GattInitializeBluetooth());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Handler batteryHandler = new Handler();
    private static final int RILEYLINK_BATTERY_UPDATE = 60 * 1000 * 15;
    private Runnable batteryTask = new Runnable() {
        @Override
        public void run() {
            /* do what you need to do */
            BluetoothConnection conn = BluetoothConnection.getInstance();

            conn.queue(new GattCharacteristicReadOperation(
                    UUID.fromString(GattAttributes.GLUCOSELINK_BATTERY_SERVICE),
                    UUID.fromString(GattAttributes.GLUCOSELINK_BATTERY_UUID),
                    new GattCharacteristicReadCallback() {
                        @Override
                        public void call(byte[] characteristic) {
                            Intent batteryUpdate = new Intent(Intents.RILEYLINK_BATTERY_UPDATE);
                            batteryUpdate.putExtra("battery", characteristic[0]);

                            LocalBroadcastManager.getInstance(context).sendBroadcast(batteryUpdate);
                        }
                    }
            ));

            if (conn.connected()) {
                batteryHandler.postDelayed(this, RILEYLINK_BATTERY_UPDATE);
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Everything here is run on a single background thread, one request at a time:
            String srq;
            srq = intent.getStringExtra("srq");
            if (srq == null) {
                srq = "(null)";
            }
            Log.w(TAG, String.format("onHandleIntent: received request srq=%s", srq));

            switch (srq) {
                case Constants.SRQ.BLUETOOTH_WRITE: {

                    BluetoothConnection conn = BluetoothConnection.getInstance();
                    byte[] command = Commands.getReadPumpCommand(new byte[]{0x41, 0x75, 0x40});

                    conn.queue(new GattCharacteristicWriteOperation(
                            UUID.fromString(GattAttributes.GLUCOSELINK_RILEYLINK_SERVICE),
                            UUID.fromString(GattAttributes.GLUCOSELINK_TX_PACKET_UUID),
                            command,
                            true,
                            true
                    ));

                    conn.queue(new GattCharacteristicWriteOperation(
                            UUID.fromString(GattAttributes.GLUCOSELINK_RILEYLINK_SERVICE),
                            UUID.fromString(GattAttributes.GLUCOSELINK_TX_TRIGGER_UUID),
                            new byte[]{0x01},
                            false,
                            false
                    ));

                    break;
                }
                case Constants.SRQ.BLUETOOTH_READ: {
                    BluetoothConnection conn = BluetoothConnection.getInstance();
                    conn.queue(new GattCharacteristicReadOperation(
                            UUID.fromString(GattAttributes.GLUCOSELINK_RILEYLINK_SERVICE),
                            UUID.fromString(GattAttributes.GLUCOSELINK_PACKET_COUNT),
                            null
                    ));

                    break;
                }
            }

        } finally {

        }

    }
}
