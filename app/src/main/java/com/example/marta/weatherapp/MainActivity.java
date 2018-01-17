package com.example.marta.weatherapp;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.marta.weatherapp.data.Channel;
import com.example.marta.weatherapp.data.Forecast;
import com.example.marta.weatherapp.data.Item;
import com.example.marta.weatherapp.service.WeatherServiceCallback;
import com.example.marta.weatherapp.service.YahooWeatherService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity implements WeatherServiceCallback, RecyclerViewClickListener {

    private ImageView weatherIconImageView;
    private TextView temperatureTextView;
    private TextView conditionTextView;
    private TextView dateTextView;
    private ImageView weatherForecastIconImageView;
    private TextView temperatureHighForecastTextView;
    private TextView temperatureLowForecastTextView;
    private TextView temperatureHighTextView;
    private TextView temperatureLowTextView;
    private TextView conditionForecastTextView;
    private TextView dateForecastTextView;
    private TextView dailyForecastTextView;
    private TextView currentArduinoTextView;
    private TextView arduinoTempTxt;
    private TextView arduinoTemp;
    private TextView arduinoHumTxt;
    private TextView arduinoHum;

    private EditText locationEditText;

    private YahooWeatherService service;
    private ProgressDialog dialog;
    String city;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private static final int MY_PERMISSION_REQUEST_LOCATION = 1;
    Forecast forecast;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initViews();
        setFonts();

        service = new YahooWeatherService(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        findCity();
        service.refreshWeather(city);

        locationEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    EditText myEditText = (EditText) view;
                    city = myEditText.getText().toString();
                    service.refreshWeather(city);
                    return true;
                }
                return false;
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        setCurrentArduinoData();
    }

    private void setCurrentArduinoData(){
        findBT();
        try {
            openBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Log.d("Bluetooth", "Name: "+"No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("WEATHER_ST"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d("BLU", "Name: "+"Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();
        Log.d("BLU", "Name: "+"Bluetooth Opened");
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {

                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Log.e("BLU", "Name: "+data);
                                            arduinoTemp.setText(data.substring(13,15)+"\u00B0"+"C");
                                            arduinoHum.setText(data.substring(29,31)+"%");

                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationEditText.setText(city);
        service.refreshWeather(city);
        //beginListenForData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        locationEditText.setText(city);
        service.refreshWeather(city);
        //beginListenForData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationEditText.setText(city);
        service.refreshWeather(city);
        //beginListenForData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //beginListenForData();
    }

    private void initViews(){
        locationEditText = (EditText) findViewById(R.id.locationEditText);
        weatherIconImageView = (ImageView) findViewById(R.id.weatherIconImageView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        conditionTextView = (TextView) findViewById(R.id.conditionTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        weatherForecastIconImageView = (ImageView) findViewById(R.id.weatherForecastIconImageView);
        temperatureHighForecastTextView = (TextView) findViewById(R.id.temperatureHighForecastTextView);
        temperatureLowForecastTextView = (TextView) findViewById(R.id.temperatureLowForecastTextView);
        temperatureHighTextView = (TextView) findViewById(R.id.temperatureHighTextView);
        temperatureLowTextView = (TextView) findViewById(R.id.temperatureLowTextView);
        conditionForecastTextView = (TextView) findViewById(R.id.conditionForecastTextView);
        dateForecastTextView = (TextView) findViewById(R.id.dateForecastTextView);
        dailyForecastTextView = (TextView) findViewById(R.id.dailyForecastTextView);
        currentArduinoTextView = (TextView) findViewById(R.id.current_arduino_data_txt);
        arduinoTempTxt = (TextView) findViewById(R.id.arduino_temp_txt);
        arduinoTemp = (TextView) findViewById(R.id.arduino_temp);
        arduinoHumTxt = (TextView) findViewById(R.id.arduino_hum_txt);
        arduinoHum = (TextView) findViewById(R.id.arduino_hum);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    public void findCity(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_REQUEST_LOCATION);
            }else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_REQUEST_LOCATION);
            }
        }else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                city = hereLocation(location.getLatitude(), location.getLongitude()).toString();
                Log.d("CITY", ""+city);
                locationEditText.setText(city);
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_LOCATION:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        try {
                            locationEditText.setText(hereLocation(location.getLatitude(), location.getLongitude()));
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Not found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public String hereLocation(double lat, double lon){
        String curCity = "";

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addressList;
        try{
            addressList = geocoder.getFromLocation(lat, lon, 1);
            if (addressList.size() > 0){
                curCity = addressList.get(0).getLocality().toString();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return curCity;
    }

    public void setFonts(){
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/abelregular.ttf");
        locationEditText.setTypeface(custom_font);
        temperatureTextView.setTypeface(custom_font);
        conditionTextView.setTypeface(custom_font);
        dateTextView.setTypeface(custom_font);
        dailyForecastTextView.setTypeface(custom_font);
        temperatureHighForecastTextView.setTypeface(custom_font);
        temperatureLowForecastTextView.setTypeface(custom_font);
        conditionForecastTextView.setTypeface(custom_font);
        dateForecastTextView.setTypeface(custom_font);
        currentArduinoTextView.setTypeface(custom_font);
        arduinoTempTxt.setTypeface(custom_font);
        arduinoTemp.setTypeface(custom_font);
        arduinoHumTxt.setTypeface(custom_font);
        arduinoHum.setTypeface(custom_font);
    }

    @Override
    public void serviceSuccess(Channel channel) {
        dialog.hide();

        Item item = channel.getItem();
        int resource = getResources().getIdentifier("drawable/icon_"+ item.getCondition().getCode(), null, getPackageName());
        int resourceForecast = getResources().getIdentifier("drawable/icon_"+ item.getForecast().getCode(1), null, getPackageName());

        @SuppressWarnings("deprecation")
        Drawable weatherIconDrawable = getResources().getDrawable(resource);
        Drawable weatherForecastIconDrawable = getResources().getDrawable(resourceForecast);

        dateTextView.setText(item.getCondition().getDate().substring(0,17));
        weatherIconImageView.setImageDrawable(weatherIconDrawable);
        temperatureTextView.setText(item.getCondition().getTemperature()+"\u00B0"+channel.getUnits().getTemperature());
        conditionTextView.setText(item.getCondition().getDescription());
        temperatureHighTextView.setText(item.getForecast().getTempHigh(0)+"\u00B0"+channel.getUnits().getTemperature());
        temperatureLowTextView.setText(item.getForecast().getTempLow(0)+"\u00B0"+channel.getUnits().getTemperature());

        dateForecastTextView.setText(item.getForecast().getDay(1)+", "+item.getForecast().getDate(1));
        weatherForecastIconImageView.setImageDrawable(weatherForecastIconDrawable);
        temperatureHighForecastTextView.setText(item.getForecast().getTempHigh(1)+"\u00B0"+channel.getUnits().getTemperature());
        temperatureLowForecastTextView.setText(item.getForecast().getTempLow(1)+"\u00B0"+channel.getUnits().getTemperature());
        conditionForecastTextView.setText(item.getForecast().getDescription(1));

        forecast = item.getForecast();
        mRecyclerView.setAdapter(new MainAdapter(forecast, this));

    }

    @Override
    public void serviceFailure(Exception exception) {
        dialog.hide();
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG);
    }

    @Override
    public void recyclerViewListClicked(View v, int position, Forecast forecast) {
        int resourceForecast = getResources().getIdentifier("drawable/icon_"+ forecast.getCode(position), null, getPackageName());
        Drawable weatherForecastIconDrawable = getResources().getDrawable(resourceForecast);
        dateForecastTextView.setText(forecast.getDay(position)+", "+forecast.getDate(position));
        weatherForecastIconImageView.setImageDrawable(weatherForecastIconDrawable);
        temperatureHighForecastTextView.setText(forecast.getTempHigh(position)+"\u00B0"+"C");
        temperatureLowForecastTextView.setText(forecast.getTempLow(position)+"\u00B0"+"C");
        conditionForecastTextView.setText(forecast.getDescription(position));
    }
}
