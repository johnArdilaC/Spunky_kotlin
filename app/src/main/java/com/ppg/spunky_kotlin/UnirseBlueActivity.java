package com.ppg.spunky_kotlin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by John on 30/03/2018.
 */

public class UnirseBlueActivity extends AppCompatActivity {

    Button send, listDevices;
    ListView listView;
    TextView msgBox, status;
    EditText writeMsg;

    BluetoothAdapter blueAdapter;
    BluetoothDevice[] btArray;

    SendReceived sendReceived;

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
        setContentView(R.layout.activity_unirseblue);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        findViewById();
        //getWindow().setSoftInputMode(
          //      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if(!blueAdapter.isEnabled()){
                Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BLUE);
            }
        implementListeners();

    }

    private void implementListeners() {
        System.out.println("ENTRA A IMPLEMENT LISTENERS");

        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt=blueAdapter.getBondedDevices();
                String[] strings= new String[bt.size()];
                btArray=new  BluetoothDevice[bt.size()];
                int index=0;
                System.out.println("ENTRA A LIST DEVICES");
                Log.e("LIST DEVICES ", "HELP");
                if(bt.size()>0){
                    for (BluetoothDevice device: bt){
                        btArray[index]=device;
                        strings[index]=device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();
                status.setText("Connecting");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string=String.valueOf(writeMsg.getText());
                sendReceived.write(string.getBytes());
            }
        });

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
        send=(Button) findViewById(R.id.send);
        listDevices=(Button) findViewById(R.id.btnConectate);
        listView=(ListView) findViewById(R.id.gamersList);
        msgBox=(TextView) findViewById(R.id.msg);
        status=(TextView) findViewById(R.id.status);
        writeMsg=(EditText) findViewById(R.id.writeApodo);
    }



    private  class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1){
            device=device1;
            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public  void run(){
            try {
                System.out.println("ENTRA AL TRY DE SOCKET");
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                ///OJOOOO
                sendReceived=new SendReceived(socket);
                sendReceived.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
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
    private void crearMensaje(int mensaje)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(UnirseBlueActivity.this);
        builder.setMessage(mensaje)
                .setTitle(R.string.label_informacion)
                .setNeutralButton(R.string.button_volver, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

