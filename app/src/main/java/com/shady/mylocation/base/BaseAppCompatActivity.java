package com.shady.mylocation.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class BaseAppCompatActivity extends AppCompatActivity {

    protected Fragment curFragment;
    private BaseViewBridge baseViewBridge;
    protected boolean connectedToInternet = true;
    private ConnectivityChangeReceiver mConnectivityChangeReceiver;

    public boolean isConnectedToInternet() {
        return connectedToInternet;
    }

    protected void hideKeyboard(){
        baseViewBridge.hideKeyboard();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseViewBridge = new BaseViewBridge(this);

    }

    protected void showProgressDialog() {
        baseViewBridge.showProgressDialog();
    }

    protected void hideProgressDialog() {
        baseViewBridge.hideProgressDialog();
    }

    protected void showToastMessage(String message){
        baseViewBridge.showToastMessage(message);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void addFragment(Fragment fragment, int containerID) {
        try {
            curFragment = fragment;
            String fragmentTag = fragment.getClass().getName();
            getSupportFragmentManager().beginTransaction().add(containerID, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void replaceFragment(Fragment fragment, int containerID) {
        try {
            curFragment = fragment;
            String fragmentTag = fragment.getClass().getName();
            getSupportFragmentManager().beginTransaction().replace(containerID, fragment, fragmentTag).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectivityChangeReceiver = new ConnectivityChangeReceiver();
        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle extras = intent.getExtras();
            if (extras != null) {

                if (extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
                    connectedToInternet = false;

                } else {
                    connectedToInternet = true;

                }
            }
        }
    }

}
