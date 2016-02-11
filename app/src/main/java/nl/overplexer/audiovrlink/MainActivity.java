package nl.overplexer.audiovrlink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView compassData;
    private Button btn_forward;
    private Button btn_backward;

    private DataClient client;
    // private float[] rotationMatrix;
    private int SENSOR_DELAY = 50000;
    private String IP = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        client = new DataClient();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compassData = (TextView) findViewById(R.id.CompassData);
        btn_forward = (Button) findViewById(R.id.forward);
        btn_backward = (Button) findViewById(R.id.backward);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        askIP();
    }

    private void askIP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter IP");
        builder.setMessage("current IP:" + client.getIP());

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IP = input.getText().toString();
                client.setIP(IP);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY);

    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); //prevent updates when screen is off
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] vals = event.values;
            float[] res = {(float)Math.cos(Math.toRadians(180f * vals[2])), 0f, (float)Math.sin(Math.toRadians(180f * vals[2]))};
            if(client.isReady()){
                client.send(res,getForward());
            }
            compassData.setText(res[0] + "\n" + res[1]  + "\n" + res[2]  + "\n" + getForward());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getForward() {
        float r = 0f;
        if(btn_forward.isPressed()) r += 1f;
        if(btn_backward.isPressed()) r -= 1f;
        return r;
    }
}
