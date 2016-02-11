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
    private TextView connectionStatus;
    private TextView dataStream;
    private Button btn_forward;
    private Button btn_backward;

    private DataClient client;
    private int SENSOR_DELAY = 50000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataStream = (TextView) findViewById(R.id.dataStream);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        btn_forward = (Button) findViewById(R.id.forward);
        btn_backward = (Button) findViewById(R.id.backward);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        askIP("Enter your 4-digit connection code:");
    }

    private void askIP(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create connection");
        builder.setMessage(msg);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    String IP = DataClient.parseConnectCode(input.getText().toString());
                    connectionStatus.setText("Connecting to: "+ IP);
                    client = new DataClient(IP);
                } catch (IllegalArgumentException e) {
                    askIP(e.getMessage());
                }
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
            float[] res = getForward(vals);
            if(client != null && client.isReady()){
                connectionStatus.setText("Connected to " + client.getServerIP());
                client.send(res,getMove());
                dataStream.setText(res[0] + "\n" + res[1]  + "\n" + res[2]  + "\n" + getMove());
            } else {
                dataStream.setText("");
            }

        }
    }

    private float[] getForward(float[] vals) {
        return new float[] {(float)Math.cos(Math.toRadians(180f * vals[2])), 0f, (float)Math.sin(Math.toRadians(180f * vals[2]))};
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getMove() {
        float r = 0f;
        if(btn_forward.isPressed()) r += 1f;
        if(btn_backward.isPressed()) r -= 1f;
        return r;
    }
}
