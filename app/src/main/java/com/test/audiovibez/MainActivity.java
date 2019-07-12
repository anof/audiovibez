package com.test.audiovibez;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.effect.Effect;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleRenderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType4Renderer;
import me.bogerchan.niervisualizer.renderer.line.LineRenderer;


public class MainActivity extends AppCompatActivity {

    final int STATE_PLAYING = 0;
    final int STATE_PAUSE = 1;
    final int STATE_STOP = 2;
    final int STATE_ERROR = 3;
    final int STATE_PLAYER = 4;
    final int STATE_RECORDER = 5;
    final int STATE_NOTHING = 6;


    int STATUS_AUDIO_RECORD;
    int STATUS_MEDIA_PLAYER;
    int STATUS_MANAGER;

    NierVisualizerManager visualizerManager;
    MediaPlayer player;
    AudioRecord recorder;
    SurfaceView surfaceView;
    TextView startStop;
    TextView chooseFile;
    TextView pauseResume;
    TextView micStartStop;
    Vibrator vibrator;


    final int RECORDER_SAMPLE_RATE = 44100;
    final int RECORDER_CHANNELS = 1;
    final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    final int bufferSize = 2 * AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    final byte[] buffer = new byte[bufferSize];
    final int SAMPLING_RATE = 44100;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions();

        try
        {

            initPlayer();
            initRecorder();
            initManager();
            initVibrator();
            configButtons();

            /*Player Buttons*/
            startStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(STATUS_MEDIA_PLAYER == STATE_STOP)
                    {
                        try {
                            player.prepare();
                            player.start();
                            updateManager(player.getAudioSessionId(), STATE_PLAYER);
                            startStop.setText("STOP");
                            STATUS_MEDIA_PLAYER = STATE_PLAYING;
                            STATUS_MANAGER = STATE_PLAYER;

                        }catch (Exception e){
                            STATUS_MEDIA_PLAYER = STATE_ERROR;
                            Log.d("MyLogsStartStop", e.toString());
                        }
                    }
                    else if(STATUS_MEDIA_PLAYER == STATE_PLAYING || STATUS_MEDIA_PLAYER == STATE_PAUSE)
                    {
                        player.stop();
                        visualizerManager.stop();
                        startStop.setText("START");
                        pauseResume.setText("PAUSE");
                        STATUS_MEDIA_PLAYER = STATE_STOP;
                        STATUS_MANAGER = STATE_NOTHING;
                    }
                    else if(STATUS_MEDIA_PLAYER == STATE_ERROR)
                    {
                        CharSequence text = "Something went wrong, please close the app and reopen it";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        startStop.setText("ERROR");
                    }

                }
            });

            pauseResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(STATUS_MEDIA_PLAYER == STATE_PLAYING)
                    {
                        player.pause();
                        visualizerManager.pause();
                        STATUS_MEDIA_PLAYER = STATE_PAUSE;
                        pauseResume.setText("RESUME");
                    }
                    else if(STATUS_MEDIA_PLAYER == STATE_PAUSE)
                    {
                        player.start();
                        visualizerManager.resume();
                        STATUS_MEDIA_PLAYER = STATE_PLAYING;
                        pauseResume.setText("PAUSE");
                    }
                    else if(STATUS_MEDIA_PLAYER == STATE_ERROR)
                    {
                        CharSequence text = "Something went wrong, please close the app and reopen it";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        startStop.setText("ERROR");
                    }

                }
            });

            /*Recorder Buttons*/
            micStartStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(STATUS_AUDIO_RECORD == STATE_STOP)
                    {
                        recorder.startRecording();
                        updateManager(recorder.getAudioSessionId(), STATE_RECORDER);
                        micStartStop.setText("STOP");
                        STATUS_AUDIO_RECORD = STATE_PLAYING;
                    }
                    else if(STATUS_AUDIO_RECORD == STATE_PLAYING)
                    {
                        recorder.stop();
                        visualizerManager.stop();
                        micStartStop.setText("START");
                        STATUS_AUDIO_RECORD = STATE_STOP;
                    }
                }
            });


        }
        catch (Exception e)
        {
            Log.d("MyLogsMain", e.toString());
        }

    }// end main

    public void initPlayer()
    {

        try {
            player = new MediaPlayer();
            player. setDataSource(getResources().openRawResourceFd(R.raw.demo_audio));
//            player.setDataSource("/storage/326C-1D15/ambulance.mp3");
            STATUS_MEDIA_PLAYER = STATE_STOP;

        }catch (Exception e)
        {
            STATUS_MEDIA_PLAYER = STATE_ERROR;
            Log.d("MyLogsStartStop", e.toString());
        }
    }

    public void initRecorder()
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT, bufferSize);

        STATUS_AUDIO_RECORD = STATE_STOP;

    }

    public void initManager()
    {
        visualizerManager = new NierVisualizerManager();
        STATUS_MANAGER = STATE_NOTHING;
    }

    public void initVibrator()
    {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void updateManager(int session, int state)
    {
        visualizerManager.release();
        initManager();
        int vState = -1;


        if(state == STATE_PLAYER) {
            vState = visualizerManager.init(session);
        }
        else if(state == STATE_RECORDER)
        {

            vState = visualizerManager.init(new NierVisualizerManager.NVDataSource() {

                byte[] mBuffer = new byte[512];
                byte[] recorderByteBuffer = new byte[bufferSize/2];
                int audioLength = (int) (recorderByteBuffer.length * 1000F / SAMPLING_RATE);




                /**
                 * Tell the manager about the data sampling interval.
                 * @return the data sampling interval which is millisecond of unit.
                 */
                @Override
                public long getDataSamplingInterval() {
                    return 0L;
                }

                /**
                 * Tell the manager about the data length of fft data or wave data.
                 * @return the data length of fft data or wave data.
                 */
                @Override
                public int getDataLength() {
                    return mBuffer.length;
                }

                /**
                 * The manager will fetch fft data by it.
                 * @return the fft data, null will be ignored by the manager.
                 */
                @Nullable
                @Override
                public byte[] fetchFftData() {
                    return null;
                }

                /**
                 * The manager will fetch wave data by it.
                 * @return the wave data, null will be ignored by the manager.
                 */
                @Nullable
                @Override
                public byte[] fetchWaveData() {
                    if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                        return null;
                    }

                    for(int i = 0; i < recorderByteBuffer.length; i++)
                        recorderByteBuffer[i] = 0;

                    recorder.read(recorderByteBuffer, 0, recorderByteBuffer.length);

                    vibrate(recorderByteBuffer);

                    int tempCounter = 0;
                    int addValue = recorderByteBuffer.length / (audioLength + mBuffer.length);
                    for (int i = 0; i <= recorderByteBuffer.length - 1 ; i += addValue) {
                        if (tempCounter >= mBuffer.length) {
                            break;
                        }

                        mBuffer[tempCounter++] = recorderByteBuffer[i];
                    }

                    return mBuffer;
                }
            });

        }

        if (vState == NierVisualizerManager.SUCCESS) {


            visualizerManager.start(surfaceView, new IRenderer[]{new ColumnarType1Renderer()});
//            visualizerManager.start(surfaceView, new IRenderer[]{new CircleBarRenderer()});

        } else {
            CharSequence text = "Something went wrong with visual manager, please Stop and play audio";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

        }

    }

    public void vibrate(byte[] audioData)
    {
        int RMS = computeRMS(audioData);
        int mapped = mappedValue(RMS);




        Log.d("MyLogs", "RMS: " + mapped);
        long[] time = new long[]{100};
        int[] pattern = new int[]{mapped};
        vibrator.vibrate(VibrationEffect.createWaveform(time, pattern, -1));


    }

    public int computeRMS(byte[] audioData)
    {
        /** Computes the RMS volume of a group of signal sizes */
        long lSum = 0;
        for (int i = 0; i < audioData.length; i++) {
            lSum = lSum + audioData[i] * audioData[i];
        }

        double avg = lSum / audioData.length;
        double rootMeanSquare = Math.sqrt(avg);

        int value = computePosValueOnly(rootMeanSquare);

        return value;
    }

    public int computePosValueOnly(double RMS)
    {
        int value;
        int raw = (int) RMS;

        value = raw - 127;
        value = Math.abs(value);

        return value;
    }

    public int mappedValue(int RMS)
    {
        int max = 255;

        double value;

        value = (RMS/30.0) * (max);

        value *= 2;

        value = (value > max)? max: value;
        value = (value < 35)? 0: value;


        return (int) value;
    }

    public void configButtons()
    {
        startStop = findViewById(R.id.tv_media_player_start_or_stop);
        chooseFile = findViewById(R.id.tv_choose_file);
        pauseResume = findViewById(R.id.tv_media_player_pause_or_resume);
        micStartStop = findViewById(R.id.tv_audio_record_start_or_stop);

        surfaceView = findViewById(R.id.sv_wave);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public void permissions()
    {

        String[] requiredPermissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.VIBRATE
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 3);
        }
    }
}
