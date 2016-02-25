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
import android.widget.RadioGroup;
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
    private RadioGroup orientationSelector;

    private DataClient client;
    private SensorListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        dataStream = (TextView) findViewById(R.id.dataStream);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        btn_forward = (Button) findViewById(R.id.forward);
        btn_backward = (Button) findViewById(R.id.backward);
        orientationSelector = (RadioGroup) findViewById(R.id.orientationSelector);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorListener = new SensorListener(sensorManager, SENSOR_DELAY);
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
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float [] res = sensorListener.getForward(getOrientation());
            if(client != null && client.isReady()){
                connectionStatus.setText(getString(R.string.connectedTo, client.getServerIP()));
                client.send(res,getMove());
                dataStream.setText(res[0] + "\n" + res[1]  + "\n" + res[2]  + "\n" + getMove());
            } else {
                dataStream.setText("");
            }
        }
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
     * Close the current connection and connect to the same ip again.
     * @param view the view that called this method.
     */
    public void reconnect(View view) {
        if(client != null) {
            client.close();
            String ip = client.getServerIP();
            client = new DataClient(ip);
        }
    }

    /**
     * Close the current connection and connect to a new one.
     * @param view the view that called this method.
     */
    public void newConnect(View view) {
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

    /**
     * Get the orientation from the radio buttons.
     * @return the corresponding value of the Orientation enum
     */
    public Orientation getOrientation() {
        int id =  orientationSelector.getCheckedRadioButtonId();
        int x = orientationSelector.indexOfChild(findViewById(id));
        return Orientation.values()[x];
    }
}
