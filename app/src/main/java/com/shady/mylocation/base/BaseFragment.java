package com.shady.mylocation.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class BaseFragment extends Fragment {


    private BaseViewBridge baseViewBridge;
    private static final int REQUEST_CALL_PHONE = 1245;
    protected final int REQ_PERMISSION = 111;
    protected final int REQUEST_CHECK_SETTINGS = 1000;

    protected void showProgressDialog() {
        baseViewBridge.showProgressDialog();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseViewBridge = new BaseViewBridge(getActivity());

    }

    protected void hideKeyboard() {
        baseViewBridge.hideKeyboard();
    }

    protected void hideProgressDialog() {
        baseViewBridge.hideProgressDialog();
    }

    protected void showToastMessage(String message) {
        baseViewBridge.showToastMessage(message);
    }

    protected void showDialogMessage(String message) {
        baseViewBridge.showDialogMessage(message);
    }

    @Override
    public void onStop() {
        super.onStop();
        baseViewBridge.hideProgressDialog();
    }

    public boolean isConnectedToInternet() {
        return baseViewBridge.isConnectedToInternet();
    }

    protected void showKeybad() {
        baseViewBridge.showKeybad();
    }

}


