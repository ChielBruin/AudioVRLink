package nl.overplexer.audiovrlink;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class that reads the sensors and converts the raw data to a forward vector.
 */
public class SensorListener implements SensorEventListener {

    private float[] gravity;
    private float[] magnet;
    private float[] rotationVector;

    private float[] oldRotation;

    public SensorListener(SensorManager sm, int SENSOR_DELAY) {
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SENSOR_DELAY);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SENSOR_DELAY);

        rotationVector = new float[3];
        oldRotation = new float[3];
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_ROTATION_VECTOR: rotationVector = event.values; break;
            case Sensor.TYPE_ACCELEROMETER: gravity = event.values; break;
            case Sensor.TYPE_MAGNETIC_FIELD: magnet = event.values; break;
        }
    }

    /**
     * Calculates the forward vector from a given rotation matrix and the orientation of the device.
     * @param deviceOrientation the orientation of the device
     * @return the forward direction vector.
     */
    public float[] getForward(Orientation deviceOrientation) {
        float[] rotation = getOrientation();
        float[] result = new float[3];
        switch (deviceOrientation) {
            case PORTRAIT: {
                result[0] = rotation[0];
                result[1] = rotation[6];
                result[2] = rotation[3];
            } break;
            case LANDSCAPE: {
                result[0] = rotation[1];
                result[1] = rotation[7];
                result[2] = rotation[4];
            } break;
            case OTHER: {
                result[0] = rotation[2];
                result[1] = rotation[8];
                result[2] = rotation[5];
            } break;
        }
        return smooth(result);
    }

    /**
     * Average the rotation with the last measured rotation to smooth it a bit.
     * @param orientation the current orientation
     * @return a smooth version of the input.
     */
    private float[] smooth(float[] orientation) {
        float[] smooth = new float[3];
        smooth[0] = (oldRotation[0] + orientation[0]) / 2.f;
        smooth[1] = (oldRotation[1] + orientation[1]) / 2.f;
        smooth[2] = (oldRotation[2] + orientation[2]) / 2.f;
        oldRotation = orientation;
        return smooth;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Gets the 3x3 rotation matrix of the device.
     * Chooses the best method to get it.
     * @return The 3x3 rotation matrix
     */
    private float[] getOrientation() {
        float[] matrix = new float[9];
        if(gravity != null && magnet != null) {
            SensorManager.getRotationMatrix(matrix, null, gravity, magnet);
        } else if(rotationVector != null) {
            SensorManager.getRotationMatrixFromVector(matrix, rotationVector);
        }
        return matrix;
    }
}

/**
 * Enum for the selected rotation.
 * LANDSCAPE works for flat and landscape, PORTRAIT for flat and portrait and OTHER works for landscape and portrait.
 */
enum Orientation {LANDSCAPE, PORTRAIT, OTHER}
