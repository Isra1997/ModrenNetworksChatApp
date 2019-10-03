package com.example.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

//This class listens to System broadcasts (eg:-battery low) and Custom broadcasts(eg:-update of user details)
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager pmanger;
    private WifiP2pManager.Channel pchannel;
    private MainActivity pActivity;


    public WifiDirectBroadcastReceiver(WifiP2pManager pmanger, WifiP2pManager.Channel pchannel, MainActivity pActivity){
        this.pmanger=pmanger;
        this.pActivity= pActivity;
        this.pchannel=pchannel;
    }

    //listens to different intents that happen during the WiFi direct P2P connection
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                //Display's an alert when the wifi is turned on or off
                Toast.makeText(context,"WiFi is On",Toast.LENGTH_SHORT).show();
            }
            else {
                //Display's an alert when the wifi is turned on or off clear
                Toast.makeText(context,"WiFi is Off",Toast.LENGTH_SHORT).show();
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            System.out.println("Peer change!");
            if (pmanger!=null){
                pmanger.requestPeers(pchannel,pActivity.peerListListener);

            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }
}
