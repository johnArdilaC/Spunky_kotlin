package com.ppg.spunky_kotlin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by John on 01/04/2018.
 */



public class EnviarRetoActivity extends AppCompatActivity {

    Button sendReto;
    EditText writeReto, writeRetoGiro;

    private SensorManager sensorManager;
    private Sensor gyroscope;
    private SensorEventListener gyroscopeEventListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifySensorExistence(gyroscope);
        findViewById();
        listener();
    }


    private void implementListeners() {
        sendReto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string=String.valueOf(writeReto.getText());
                if(string.trim().equals("")){
                    createMessage(R.string.label_errorReto);
                }
                else{
                    toastMessage();
                    long inicio = System.currentTimeMillis();
                    long terminacion = inicio + 6*1000; // 600 segundos * 1000 millisegundos
                    if (System.currentTimeMillis() > terminacion)
                    {
                        launchRecord();
                    }
                }

            }
        });



    }


    private void findViewById() {
        //help=(Button)findViewById(R.id.btnHelp);
        sendReto=(Button)findViewById(R.id.btnSendReto);
        writeReto=(EditText) findViewById(R.id.writeReto);
        writeRetoGiro=(EditText) findViewById(R.id.writeRetoGiro);
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscope=sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


    }

    private void verifySensorExistence(Sensor gyroscope){
        if(gyroscope==null){
            setContentView(R.layout.activity_retoenviado);
            implementListeners();
        }
        else{
            setContentView(R.layout.activity_retoenviado_giroscopio);
        }
    }

    private void listener(){
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String string=String.valueOf(writeReto.getText());
                if(string.trim().equals("")){
                    createMessage(R.string.label_errorReto);
                }
                else{
                    if (sensorEvent.values[2] < -1.5f) {
                        toastMessage();
                        long inicio = System.currentTimeMillis();
                        long terminacion = inicio + 6*1000; // 600 segundos * 1000 millisegundos
                        if (System.currentTimeMillis() > terminacion)
                        {
                            launchRecord();
                        }
                    }
                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener,gyroscope,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);
    }

    private void createMessage(int mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EnviarRetoActivity.this);
        builder.setMessage(mensaje)
                .setTitle("Error")
                .setNeutralButton(R.string.button_volver, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void toastMessage(){
        Context context = getApplicationContext();
        CharSequence text = "Reto Enviado!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void launchRecord() {
        Intent intent = new Intent(this, RecordMemoryActivity.class);
        startActivity(intent);
    }
}
