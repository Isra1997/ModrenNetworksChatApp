package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

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
    WifiP2pManager p2pManager;
    WifiP2pManager.Channel p2pChannel;
    MainActivity p2pMainactivity;

    //BroadcastReceiver Variables
    BroadcastReceiver p2pReceiver;
    IntentFilter p2pIntentFilter;

    //List of devices connected
    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    String[] DeviceNames;
    WifiP2pDevice[] DeviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnStart();
        btnListener();
    }

    private void btnListener() {
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
                    btnOnOff.setText("Wifi OFF");
                }
            }
        });

        //Adding a listener  to the discover devices
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Discovering nearby peers
                p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {

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
        p2pManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel=p2pManager.initialize(this,getMainLooper(),null);

        //initialization of Wifi Direct Broadcast Receiver
        p2pReceiver=new WifiDirectBroadcastReceiver(p2pManager,p2pChannel,this);

        //initializing the intent filter
        p2pIntentFilter=new IntentFilter();

        //adding actions to listen to to the intents received by the Wifi direct API
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    //Called when an activity resumes after being paused
    protected void onResume() {
        super.onResume();
        registerReceiver(p2pReceiver,p2pIntentFilter);
    }

    //Called when an activity is hidden from view
    protected void onPause() {
        super.onPause();
        unregisterReceiver(p2pReceiver);
    }
}
