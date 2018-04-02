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
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EnviarRetoActivity extends AppCompatActivity {

    Button sendReto;
    EditText writeReto;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_retoenviado);
        findViewById();
        implementListeners();
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
                    Delay(2500);
                }
            }
        });
    }

    private void findViewById() {
        sendReto=(Button)findViewById(R.id.btnSendReto);
        writeReto=(EditText) findViewById(R.id.writeReto);
    }


    public void Delay(int milisegundos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                launchRecord();
            }
        }, milisegundos);
    }
    //////////////////
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
