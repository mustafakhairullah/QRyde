package com.example.qryde;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * This class describes the app activeity when a driver
 * scans a riders QRcode to transfer QRBucks
 * This class is not yet completed!!
 */

public class ScanQRCode extends AppCompatActivity{
    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner);
        surfaceView =  findViewById(R.id.cameraView);
        textView =  findViewById(R.id.txtContext);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(this,barcodeDetector)
                .setRequestedPreviewSize(640,480).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){
            /**
             * This creates the QR code
             * @param holder a SurfaceHolder object
             */
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

                    return;
                }
                try {
                    cameraSource.start(holder);
                }catch(IOException e){
                    e.printStackTrace();
                }


            }
            /**
             * This changes the format of the surface holder already generated to new specified
             * @param holder
             * @param format
             * @param width
             * @param height
             */
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder){
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>(){
            @Override

            public void release(){

            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections){
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if(qrCodes.size() != 0)
                {
                    textView.post(new Runnable() {
                        @Override
                        public void run(){
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            textView.setText(qrCodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });







    }


}
