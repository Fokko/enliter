package frl.driesprong.enliter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import frl.driesprong.Constants;
import frl.driesprong.Intents;
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
            TextView textView = (TextView)findViewById( R.id.default_text);

            if (intent.getAction().equals(Intents.ENLITE_SENSOR_UPDATE)) {
                textView.setText("Update");
            } else if (intent.getAction().equals(Intents.RILEYLINK_BATTERY_UPDATE)) {
                textView.setText("Battery update");
            } else if (intent.getAction().equals(Intents.RILEYLINK_CONNECTED)) {
                textView.setText("Connected");
            } else if (intent.getAction().equals(Intents.RILEYLINK_CONNECTING)) {
                textView.setText("Connecting");
            } else if (intent.getAction().equals(Intents.RILEYLINK_DISCONNECTED)) {
                textView.setText("Disconnected");
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
