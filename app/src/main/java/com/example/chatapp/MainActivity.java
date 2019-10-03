package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.InetAddresses;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //View variables
    Button btnOnOff, btnDiscover,btnSend;
    ListView listView;
    TextView read_msg_box, conncection_status;
    EditText write_msg;
    WifiManager manger;

    //Wifi direct P2P manger variables
    WifiP2pManager pManager;
    WifiP2pManager.Channel pChannel;
    MainActivity pMainactivity;

    //BroadcastReceiver Variables
    BroadcastReceiver pReceiver;
    IntentFilter pIntentFilter;

    //List of devices connected
    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    String[] DeviceNames;
    WifiP2pDevice[] DeviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnStart();
        Listeners();
    }

    private void Listeners() {
        if (manger.isWifiEnabled()){
            btnOnOff.setText("Wifi Off");
        }
        else {
            btnOnOff.setText("Wifi On");
        }
        //Adding a button listener to turn the wifi on and off
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(manger.isWifiEnabled()){
                    manger.setWifiEnabled(false);
                    btnOnOff.setText("Wifi On");
                }
                else {
                    manger.setWifiEnabled(true);
                    btnOnOff.setText("Wifi Off");
                }
            }
        });

        //Adding a listener  to the discover devices
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Discovering nearby peers
                pManager.discoverPeers(pChannel, new WifiP2pManager.ActionListener() {

                    //Called when discovery starts successfully
                    public void onSuccess() {
                        conncection_status.setText("Discovery Started!");
                    }

                    //Called discovery fails
                    public void onFailure(int i) {
                        conncection_status.setText("Discovery Failed!");
                    }
                });
            }
        });

        //Listing for the connection to a new client
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Getting the clicked device from the array
                final WifiP2pDevice device=DeviceArray[i];
                //Creating a new config instance
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;

                //Wifi P2p manager instantiating a connection
                pManager.connect(pChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Unable to connect to the request device",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //Initializing the App view,wifi manager,wifi p2p manager,intent filter and adding actions to the intents
    private void OnStart(){
        btnOnOff=(Button) findViewById(R.id.onOff);
        btnDiscover=(Button) findViewById(R.id.discover);
        btnSend=(Button) findViewById(R.id.sendButton);
        listView=(ListView) findViewById(R.id.peerListView);
        read_msg_box=(TextView) findViewById(R.id.readMsg);
        conncection_status=(TextView) findViewById(R.id.connectionStatus);
        write_msg=(EditText)findViewById(R.id.writeMsg);

        manger= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //P2P channel and manger initialization
        pManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        pChannel=pManager.initialize(this,getMainLooper(),null);

        //initialization of Wifi Direct Broadcast Receiver
        pReceiver=new WifiDirectBroadcastReceiver(pManager,pChannel,this);

        //initializing the intent filter
        pIntentFilter=new IntentFilter();

        //adding actions to listen to to the intents received by the Wifi direct API
        pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    //Listening for new peers
    WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                //Clearing then adding the peer list
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                //Clearing the device names array and device array
                DeviceNames=new String[peerList.getDeviceList().size()];
                DeviceArray=new WifiP2pDevice[peerList.getDeviceList().size()];

                //Adding the peer list to the device name array and device array
                int i=0;
                for (WifiP2pDevice device: peerList.getDeviceList()){
                    DeviceNames[i]=device.deviceName;
                    DeviceArray[i]=device;
                    i++;
                }

                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,DeviceNames);
                listView.setAdapter(adapter);
            }

            if (peerList.getDeviceList().size()==0){
                Toast.makeText(getApplicationContext(),"No Devices Discovered",Toast.LENGTH_SHORT).show();
            }

        }
    };

    //Listening to the connection information after successful connection
    WifiP2pManager.ConnectionInfoListener ConnectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            //get the ip address of the group owner
            final InetAddress groupOwnerAddress= wifiP2pInfo.groupOwnerAddress;

            //checking is the device is the group owner to see who is the host and who is the client
            if (wifiP2pInfo.isGroupOwner && wifiP2pInfo.groupFormed){
                conncection_status.setText("Host device");
            } else if (wifiP2pInfo.groupFormed){
                conncection_status.setText("Client device");
            }
        }
    };

    //Called when an activity resumes after being paused
    protected void onResume() {
        super.onResume();
        registerReceiver(pReceiver,pIntentFilter);
    }

    //Called when an activity is hidden from view
    protected void onPause() {
        super.onPause();
        unregisterReceiver(pReceiver);
    }

    public class HostClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class  ClientClass extends Thread{
        Socket socket;
        String HostName;

        public ClientClass(InetAddress HostAddress){
            this.HostName=HostAddress.getHostName();
            socket=new Socket();
        }
        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(HostName,8888),500);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
