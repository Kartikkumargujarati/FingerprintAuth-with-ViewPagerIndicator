package com.kartik.newmalauzaiproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Kartik on 2/22/2017.
 */
public class AuthenticationHandler extends FingerprintManager.AuthenticationCallback {
    ImagesActivity imagesActivity;
    Context context;
    CancellationSignal cancellationSignal;
    public AuthenticationHandler(Context context){
        this.context = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Verification failed. Try Again.!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Toast.makeText(context, "Verification successful", Toast.LENGTH_LONG).show();
        context.startActivity(new Intent(context, ImagesActivity.class));

    }
}
