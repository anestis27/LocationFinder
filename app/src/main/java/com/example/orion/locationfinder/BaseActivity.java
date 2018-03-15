package com.example.orion.locationfinder;

/**
 * Created by Orion on 15/03/2018.
 */


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public void goToActivity(Class<?> toActivity) {
        Intent intent = new Intent(this, toActivity);
        startActivity(intent);
    }

    public void goToActivity(Class<?> toActivity, boolean finishCurrent) {
        goToActivity(toActivity);
        if(finishCurrent) finish();
    }

}