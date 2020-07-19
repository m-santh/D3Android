package com.modec.d3;

import android.Manifest;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mitchwongho.d3.R;
import com.modec.d3.domain.DataPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.BindView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final double SPEED_SENSITIVITY_LOW = 0.01f; //meters per second https://www.healthline.com/health/exercise-fitness/average-walking-speed#average-speed-by-age
    private static final double SPEED_SENSITIVITY_MAX = 5.0f;

    private static  double ACCEL_SENSITIVITY_LOW = 0.1f; // for 100 millisecond
    private static  double ACCEL_SENSITIVITY_MAX = 50.0f;

    private static  double DIST_SENSITIVITY_LOW = 0.0001f; // for 100 millisecond
    private static  double DIST_SENSITIVITY_MAX = 150.0f;

    private static final int SENSOR_OF_INTEREST = Sensor.TYPE_LINEAR_ACCELERATION;

    @BindView( R.id.webview)
    WebView webview;

    //private JSONObject trace;
    private JSONArray xValue = new JSONArray(), yValue = new JSONArray(), zValue = new JSONArray();

    private SensorManager mSMan;

    //private double [] currentValues = new double[3];
    //private double [] prevValues = new double[3];
    private double [] sumValues = new double[3];
    private int      valueCount = 0;
    private double [] referencePoint = new double[3];
    private double prevLinearAccTimeStamp = 0.0F;

    private double prevAccel = 0.0f, prevSpeed = 0.0f, prevDistance = 0.0f;



    public class WebAppInterface {
        private Context context;

        public WebAppInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public JSONObject loadData() {
//            return "[{\"letter\": \"A\", \"frequency\": \".09\" },{\"letter\": \"B\", \"frequency\": \".05\" }]";
            try {
                return createDataSet();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private final JSONObject createDataSet() throws JSONException{

            JSONObject data = new JSONObject();
            data.put("x", yValue);
            data.put("y", zValue);
            data.put("z", xValue);

            return data;
            /*
            final Random rand = new Random(System.currentTimeMillis());
            final String[] x = new String[] {
                    "A", "B", "C", "D", "E", "F",
                    "G", "H", "I", "J", "K", "L",
                    "M", "N", "O", "P", "Q", "R",
                    "S", "T", "U", "V", "W", "X",
                    "Y", "Z"};
            final List<DataPoint> set = new ArrayList<DataPoint>();
            for (int i = 0; i < x.length ; i++) {
                set.add( new DataPoint(x[i], rand.nextFloat()));
            }
            final DataPoint[] pts = set.toArray( new DataPoint[]{} );
            return new Gson().toJson(pts, DataPoint[].class );

             */
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
    }

    protected void addPoint(double [] distance) throws JSONException {

        xValue.put((double)distance[0]);
        yValue.put((double)distance[1]);
        zValue.put((double)distance[2]);

        JSONObject data = new JSONObject();
        data.put("x", xValue);
        data.put("y", zValue);
        data.put("z", yValue);

        Log.d(TAG, " distanceX: " + prevAccel );
        Log.d(TAG, " distanceY: " + prevSpeed );
        Log.d(TAG, " DistanceZ: " + prevDistance );

        webview.loadUrl("javascript:PlotlyUpdate('"+data+"')");
    }

    public double[] calculateDistance (double[] acceleration, double deltaTime) {
        double[] distance = new double[acceleration.length];
        double[] speed = new double[acceleration.length];

        for (int i = 0; i < acceleration.length; i++) {
            speed[i] = acceleration[i] * deltaTime;
            distance[i] = (speed[i] * deltaTime)/1000;
        }

        return distance;
    }

    public double calculateEquiDistance(double[] acceleration) {
        double sqrSum = 0;
        for (int i = 0; i < acceleration.length; i++) {
            sqrSum = sqrSum + acceleration[i] * acceleration[i];
        }
        
        return Math.sqrt(sqrSum);
    }
    
    public void onSensorChanged(SensorEvent event) {
        int etype = event.sensor.getType();
        double [] accValues;
        double currAccel = 0.0f, currSpeed = 0.0f, currDistance = 0.0f;
        //long etime = event.timestamp;

        //Recod the value and time
        switch (etype) {
            case SENSOR_OF_INTEREST:

                //prevValues = currentValues;
                //currentValues = event.values;
                accValues = new double[3];
                accValues[0] = (double) event.values[0];
                accValues[1] = (double) event.values[0];
                accValues[2] = (double) event.values[0];

               break;
                //return;


                //break;
            case Sensor.TYPE_STEP_DETECTOR:

                return;
                /*
                accValues = new double[3];
                accValues[0] = sumValues[0]/valueCount;
                accValues[1] = sumValues[1]/valueCount;
                accValues[2] = sumValues[2]/valueCount;

                sumValues[0] = 0;
                sumValues[1] = 0;
                sumValues[2] = 0;

                valueCount = 0;

                break;

                 */
            default:
                return;
        }

        if(prevLinearAccTimeStamp <= 0.0f) {
            prevLinearAccTimeStamp = event.timestamp / 1000000000.0f;
            return;
        }

        double currTime = event.timestamp / 1000000000.0f;
        double deltaTime = currTime - prevLinearAccTimeStamp;

        prevLinearAccTimeStamp = currTime;

        currAccel = calculateEquiDistance(accValues);

        currSpeed = currAccel * deltaTime;

        currDistance = currSpeed * deltaTime;

        if( (currSpeed < SPEED_SENSITIVITY_LOW) || (currSpeed > SPEED_SENSITIVITY_MAX) || (currAccel < SPEED_SENSITIVITY_LOW)) {
            // dont update as it is zero accelerator
            //prevLinearAccTimeStamp = event.timestamp / 1000000000.0f;
            return;
        }
        sumValues[0] = sumValues[0] + (double)event.values[0];
        sumValues[1] = sumValues[1] + (double)event.values[1];
        sumValues[2] = sumValues[2] + (double)event.values[2];

        valueCount = valueCount + 1;


        double[] distance = calculateDistance(accValues, deltaTime);
        //currDistance = calculateEquiDistance(distance);
        //Debug.Log("CurrentDistance " + CurrentDistanceTravelled + "values:" + values[0] + " y: " +values[1] + " x: " +values[2]);
        if((currDistance != prevDistance || currAccel != prevAccel || currSpeed != prevSpeed) && (currDistance > DIST_SENSITIVITY_LOW)) {

            referencePoint[0] = referencePoint[0] + distance[0];
            referencePoint[1] = referencePoint[1] + distance[1];
            referencePoint[2] = referencePoint[2] + distance[2];

            prevAccel = currAccel;
            prevSpeed = currSpeed;
            prevDistance = currDistance;

            try {
                addPoint(referencePoint);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind( this );
/*
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PHYISCAL_ACTIVITY);
        }
*/
    }

    @Override
    public void onStart() {
        super.onStart();

        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        //Get a reference to sensor manager
        mSMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        mSMan.unregisterListener(this);
        int rate= 100000; // 100 millisec SensorManager.SENSOR_DELAY_NORMAL;

        //Sensors
        Sensor sAcc = mSMan.getDefaultSensor(SENSOR_OF_INTEREST);
        if (sAcc!=null) mSMan.registerListener(this, sAcc, rate);

        //Sensor sStep = mSMan.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //if (sStep!=null) mSMan.registerListener(this, sStep, rate);

        /*
        Sensor sAcc = mSMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sAcc!=null) mSMan.registerListener(this, sAcc, rate);

        Sensor sMag = mSMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sMag!=null) mSMan.registerListener(this, sMag, rate);

        Sensor sGyro = mSMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sGyro!=null) mSMan.registerListener(this, sGyro, rate);

         */

        // Initialize origin point
        //xValue.put(0);
        //yValue.put(0);
        //zValue.put(0);

        referencePoint[0] = 0;
        referencePoint[1] = 0;
        referencePoint[2] = 0;

        Log.d(TAG, "Senser Registered :" );
    }

    @Override
    protected void onResume() {
        super.onResume();

        final WebSettings ws = webview.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setPluginState(WebSettings.PluginState.ON);
        ws.setAllowFileAccess(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface( new WebAppInterface( this ), "Android");
        webview.loadUrl("file:///android_asset/main.html");
    }

    //@Override
    //protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        //super.onPause();
        //mSMan.unregisterListener(this);
    //}
}
