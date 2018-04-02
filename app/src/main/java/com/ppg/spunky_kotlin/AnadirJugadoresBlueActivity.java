package com.ppg.spunky_kotlin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by John on 30/03/2018.
 */

public class AnadirJugadoresBlueActivity extends AppCompatActivity {


    Button listen, send;
    TextView msgBox, status;
    EditText writeMsg;
    ListView listView;

    BluetoothAdapter blueAdapter;
    SendReceived sendReceived;

    Boolean esPosibleEnviar;

    static  final int STATE_LISTENING=1;
    static  final int STATE_CONNECTING=2;
    static  final int STATE_CONNECTED=3;
    static  final int STATE_CONNECTION_FAILED=4;
    static  final int STATE_MESSAGE_RECEIVED=5;

    int REQUEST_ENABLE_BLUE=1;

    private static final String APP_NAME="SPUNKY";
    private static final UUID MY_UUID= UUID.fromString("8274dce0-26a6-4596-a7d2-4fdf0e36d745");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_bluejugadores);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        findViewById();

        if(!blueAdapter.isEnabled()){
            Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BLUE);
        }

        implementListeners();
    }

    private void implementListeners() {

        listen.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
               

                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (esPosibleEnviar==true){
                String string=String.valueOf(writeMsg.getText());
                if(string.trim().equals("")){
                createMessage(R.string.label_nickname_error);
                }
                else{
                    Log.e("valor string", string);
                    sendReceived.write(string.getBytes());
                }
            }
            else{
                toastMessage();
            }

            }
        });

        //listView

    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case  STATE_CONNECTION_FAILED:
                    status.setText("Failed");
                    break;
                ///OJOOOOOOOOOO
                case STATE_MESSAGE_RECEIVED:

                    byte[] readBuffer= (byte[]) msg.obj;
                    String tempMsg=new String(readBuffer,0,msg.arg1);
                    msgBox.setText(tempMsg);

                    break;
            }

            return true;
        }
    });


    private void findViewById() {
        listen=(Button) findViewById(R.id.btnConectate);
        send=(Button) findViewById(R.id.send);
        msgBox=(TextView) findViewById(R.id.msg);
        status=(TextView) findViewById(R.id.status);
        writeMsg=(EditText) findViewById(R.id.writeApodo);
        listView=(ListView) findViewById(R.id.gamersList);
        esPosibleEnviar=false;
    }


    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket=blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public  void run(){
            BluetoothSocket socket = null;

            while (socket==null){
                try {
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null){
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    //write some code for send/recieve
                    esPosibleEnviar=true;
                    sendReceived=new SendReceived(socket);
                    sendReceived.start();

                    break;
                }
            }
        }
    }


    private class SendReceived extends  Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceived (BluetoothSocket socket){
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;

        }

        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void createMessage(int mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AnadirJugadoresBlueActivity.this);
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
        CharSequence text = "Error: No puedes enviar un apodo sin antes conectarte";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}