package com.shady.mylocation.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.shady.mylocation.R;



public class BaseViewBridge {

    private Activity mActivity;
    public ProgressDialog mProgressDialog;
    private final String TAG = BaseViewBridge.class.getName();

    public BaseViewBridge(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void showProgressDialog() {
        ProgressBar progressBar = new ProgressBar(mActivity, null,android.R.attr.progressBarStyle);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mProgressDialog.setContentView(progressBar);
    }
    public void hideKeyboard(){
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    public void showToastMessage(String message){
        if(null == message || "".equals(message) ){
            message = mActivity.getString(R.string.common_screen_general_error);
        }
        if (message.contains(mActivity.getString(R.string.no_internet))){
            Toast.makeText(mActivity, mActivity.getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
    }

    public boolean isConnectedToInternet(){
        if(mActivity instanceof BaseAppCompatActivity){
            return ((BaseAppCompatActivity) mActivity).isConnectedToInternet();
        }
        return true;
    }

    public void showKeybad(){
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void showDialogMessage(String message){
//        new AlertDialog.Builder(mActivity) //, R.style.CustomDialogStyle)
//                .setTitle(R.string.message_str)
//                .setMessage(message)
//                .setNegativeButton(R.string.ok_text, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                })
//                .setCancelable(true)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }
}
