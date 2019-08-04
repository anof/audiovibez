package com.test.audiovibez;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    final int STATE_PLAYING = 0;
    final int STATE_STOP = 1;
    int STATUS_AUDIO_RECORD = STATE_STOP;

    AudioRecorder audioRecorder;
    DeviceVibrator deviceVibrator;
    AudioVisualizer audioVisualizer;

    TextView micStartStop;
    TextView ampEnableDisable;
    boolean amplify;
    SurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions();
        configButtons();

    }// end onCreate




    public void configButtons()
    {
        micStartStop = findViewById(R.id.tv_audio_record_start_or_stop);
        ampEnableDisable = findViewById(R.id.amplifyBtn);
        surfaceView = findViewById(R.id.sv_wave);

        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        amplify = false;
    }

    public void permissions()
    {

        String[] requiredPermissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.VIBRATE
        };

        ActivityCompat.requestPermissions(this, requiredPermissions, 1);



    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try
                    {
                        audioRecorder = new AudioRecorder();
                        deviceVibrator = new DeviceVibrator(getApplicationContext());
                        audioVisualizer = new AudioVisualizer(getApplicationContext(), surfaceView, audioRecorder, deviceVibrator);


                        /*Button Listeners*/
                        // Mic button
                        micStartStop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (STATUS_AUDIO_RECORD == STATE_STOP) {

                                    audioRecorder.start();
                                    audioVisualizer.updateManager();
                                    micStartStop.setText("STOP");
                                    STATUS_AUDIO_RECORD = STATE_PLAYING;

                                } else if (STATUS_AUDIO_RECORD == STATE_PLAYING) {
                                    audioRecorder.stop();
                                    audioVisualizer.stop();
                                    micStartStop.setText("START");
                                    STATUS_AUDIO_RECORD = STATE_STOP;
                                }
                            }
                        });

                        // Amplify button
                        ampEnableDisable.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (amplify) {
                                    ampEnableDisable.setText("Amplify Disabled");
                                    ampEnableDisable.setBackgroundColor(Color.parseColor("#d9534f"));
                                    amplify = !amplify;
                                    deviceVibrator.setAmplify(amplify);
                                } else if (!amplify) {
                                    ampEnableDisable.setText("Amplify Enabled");
                                    ampEnableDisable.setBackgroundColor(Color.parseColor("#5cb85c"));
                                    amplify = !amplify;
                                    deviceVibrator.setAmplify(amplify);
                                }
                            }
                        });
                }
        catch (Exception e)
                {
                    CharSequence text = "Something went wrong! Please restart the app .. \n" +
                            "If restarting didn't work, please contact the developer";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

                }

                } else {
                    permissions();
                }
                return;
            }
        }
    } // end onRequestPermissionsResult
}
