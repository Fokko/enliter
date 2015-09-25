package frl.driesprong.enliter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import frl.driesprong.Constants;
import frl.driesprong.Intents;
import frl.driesprong.enlite.calibration.CalibrationPair;
import frl.driesprong.processing.MedtronicProcessor;
import frl.driesprong.services.EnliterService;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static Activity thisActivity;

    private static final int RESULT_SETTINGS = 1;

    public static Activity getAppContext() {
        return thisActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;

        this.startService(new Intent(this, EnliterService.class).putExtra("srq", Constants.SRQ.START_SERVICE));
    }

    // For receiving and displaying log messages from the Service thread
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "Got event: " + intent.getAction());

            if (intent.getAction().equals(Intents.ENLITE_SENSOR_UPDATE)) {

                TextView textView = (TextView) findViewById(R.id.bg_current);


            } else if (intent.getAction().equals(Intents.RILEYLINK_BATTERY_UPDATE)) {
                // Still not working due hardware error
            } else if (intent.getAction().equals(Intents.RILEYLINK_CONNECTED)) {
                TextView textView = (TextView) findViewById(R.id.bluetooth_status);
                textView.setText("Connected");
                textView.setTextColor(Color.GREEN);
            } else if (intent.getAction().equals(Intents.RILEYLINK_CONNECTING)) {
                TextView textView = (TextView) findViewById(R.id.bluetooth_status);
                textView.setText("Connecting");
                textView.setTextColor(Color.YELLOW);
            } else if (intent.getAction().equals(Intents.RILEYLINK_DISCONNECTED)) {
                TextView textView = (TextView) findViewById(R.id.bluetooth_status);
                textView.setText("Disconnected");
                textView.setTextColor(Color.RED);
            } else if (intent.getAction().equals(Intents.NEW_CALIBRATION_BG)) {
                ListView calibrationsList = (ListView) findViewById(R.id.list_calibrations);

                // Repopulate list
                ArrayList<String> items = new ArrayList<>();

                MedtronicProcessor proc = MedtronicProcessor.getInstance();
                for (CalibrationPair cal : proc.getCalibrationPairs()) {
                    items.add(cal.toString());
                }

                ArrayAdapter adapter = new ArrayAdapter<>(thisActivity, R.layout.activity_main, items);

                calibrationsList.setAdapter(adapter);

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, RESULT_SETTINGS);
            return true;
        } else if (id == R.id.action_add_calibration) {
            AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
            builder.setMessage(R.string.dialog_manual_calibration)
                    .setTitle(R.string.dialog_manual_calibration_title);

            builder.setView(thisActivity.getLayoutInflater().inflate(R.layout.dialog_add_calibration, null));

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    TextView textView = (TextView) findViewById(R.id.dialog_add_calibration_value);

                    Double bg = Double.parseDouble(textView.getText().toString());


                    MedtronicProcessor proc = MedtronicProcessor.getInstance();

                    //proc.getCalibrationPairs().add(new CalibrationPair))
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            AlertDialog dialog = builder.create();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intents.RILEYLINK_CONNECTED);
        intentFilter.addAction(Intents.RILEYLINK_CONNECTING);
        intentFilter.addAction(Intents.RILEYLINK_DISCONNECTED);
        intentFilter.addAction(Intents.RILEYLINK_BATTERY_UPDATE);
        intentFilter.addAction(Intents.ENLITE_SENSOR_UPDATE);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, intentFilter);
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
    }
}
