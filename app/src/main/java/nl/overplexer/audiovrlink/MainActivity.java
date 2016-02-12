package nl.overplexer.audiovrlink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Main activity class.
 * Makes a connection to a compatible server and sends rotation and movement data to it.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final int SENSOR_DELAY = 50000;
    private static MainActivity instance;

    private SensorManager sensorManager;
    private TextView connectionStatus;
    private TextView dataStream;
    private Button btn_forward;
    private Button btn_backward;

    private DataClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        dataStream = (TextView) findViewById(R.id.dataStream);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        btn_forward = (Button) findViewById(R.id.forward);
        btn_backward = (Button) findViewById(R.id.backward);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        askIP(getString(R.string.ccEnter));
    }

    /**
     * Ask the user to fill in either an IP address or a connect code of the server to connect to.
     * The connect code is parsed to the corresponding IP address.
     * Calls itself when the provided connect code or IP address is of the wrong format.
     * @param msg The message to display as description.
     */
    private void askIP(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.createConnection);
        builder.setMessage(msg);
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    String IP;
                    String inputText = input.getText().toString();
                    if(inputText.indexOf('.') == -1){    // If 4-digit connect code
                        IP = DataClient.parseConnectCode(inputText);
                    } else {
                        if(inputText.split("\\.").length != 4) {
                            throw new IllegalArgumentException(getString(R.string.IPsyntaxError));
                        }
                        IP = inputText;
                    }
                    connectionStatus.setText(getString(R.string.connectingTo, IP));
                    client = new DataClient(IP);
                } catch (IllegalArgumentException e) {
                    askIP(e.getMessage());
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
                connectionStatus.setText(getString(R.string.connectedTo, client.getServerIP()));
                client.send(res,getMove());
                dataStream.setText(res[0] + "\n" + res[1]  + "\n" + res[2]  + "\n" + getMove());
            } else {
                dataStream.setText("");
            }

        }
    }

    /**
     * Calculates the forward vector from a given rotation vector.
     * @param rotation The rotation vector.
     * @return the forward direction vector.
     */
    private float[] getForward(float[] rotation) {
        return new float[] {(float)Math.cos(Math.toRadians(180f * rotation[2])), 0f, (float)Math.sin(Math.toRadians(180f * rotation[2]))};
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * Get the movement of the player.
     * @return 0 if the player is not moving, -1 when the player move backwards and 1 when the player moves forward.
     */
    public float getMove() {
        float r = 0f;
        if(btn_forward.isPressed()) r += 1f;
        if(btn_backward.isPressed()) r -= 1f;
        return r;
    }

    /**
     * Close the current connection and connect to a new one.
     * @param view the view that called this method.
     */
    public void reconnect(View view) {
        connectionStatus.setText(getString(R.string.notConnected));
        if(client != null) client.close();
        askIP(getString(R.string.ccEnter));
    }

    /**
     * Get the instance of this singleton.
     * @return the instance of MainActivity
     */
    public static MainActivity getInstance() {
        return instance;
    }
}
