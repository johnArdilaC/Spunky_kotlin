package com.ppg.spunky_kotlin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by John on 30/03/2018.
 */

public class UnirseBlueActivity extends AppCompatActivity {

    private BluetoothDevice deviceServer;
    Button send, listDevices;
    ListView listView;
    TextView msgBox, status;
    EditText writeMsg;

    BluetoothAdapter blueAdapter;
    ArrayList<BluetoothDevice>btArray;

    SendReceived sendReceived;
    Boolean esPosibleEnviar;
    ArrayList nearDevices;
    ArrayAdapter arrayAdapter;

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

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            if(!blueAdapter.isEnabled()){
                Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BLUE);
            }
        implementListeners();
    }

    private void implementListeners() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ///USE THIS CODE IF YOU WANT TO KNOW THE PRIVIUSLY BONDED DEVICES
                /**
                Set<BluetoothDevice> bt=blueAdapter.getBondedDevices();

                String[] strings= new String[bt.size()];
                btArray=new  BluetoothDevice[bt.size()];
                int index=0;

                if(bt.size()>0){
                    for (BluetoothDevice device: bt){
                        btArray[index]=device;
                        strings[index]=device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    listView.setAdapter(arrayAdapter);
                }
                */

                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
                registerReceiver(broadcastReceiver, intentFilter);
                blueAdapter.startDiscovery();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String textItemList = (String) listView.getItemAtPosition(i);
                for(int j=0; j<btArray.size(); j++){
                    String name = btArray.get(j).getName();
                    if(name.equalsIgnoreCase(textItemList)){
                        deviceServer=btArray.get(j);
                    }
                }
                ClientClass clientClass = new ClientClass(deviceServer);
                clientClass.start();
                status.setText("Connecting");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(esPosibleEnviar==true){
                    String string=String.valueOf(writeMsg.getText());
                    if(string.trim().equals("")){
                        crearMensaje(R.string.label_nickname_error);
                    }else{
                        sendReceived.write(string.getBytes());
                    }
                }else{
                    toastMessage();
                }
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
        nearDevices=new ArrayList<String>();
        btArray=new ArrayList<BluetoothDevice>();
        esPosibleEnviar=false;

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
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);
                esPosibleEnviar=true;
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

    private void toastMessage(){
        Context context = getApplicationContext();
        CharSequence text = "Error: No puedes enviar un apodo sin antes conectarte";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArray.add(device);
                nearDevices.add(device.getName());
                arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,nearDevices);
                listView.setAdapter(arrayAdapter);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}

