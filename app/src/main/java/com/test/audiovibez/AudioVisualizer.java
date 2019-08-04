package com.test.audiovibez;


import android.content.Context;
import android.media.AudioRecord;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.widget.Toast;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer;

public class AudioVisualizer {
    NierVisualizerManager visualizerManager;
    Context context;
    SurfaceView surfaceView;
    AudioRecorder audioRecorder;
    DeviceVibrator deviceVibrator;
    AudioRecord recorder;

    public AudioVisualizer(Context context, SurfaceView surfaceView, AudioRecorder audioRecorder, DeviceVibrator deviceVibrator)
    {
        this.context = context;
        this.surfaceView = surfaceView;
        this.audioRecorder = audioRecorder;
        this.deviceVibrator = deviceVibrator;
        visualizerManager = new NierVisualizerManager();
        recorder = audioRecorder.getRecorder();
    }

    public NierVisualizerManager getVisualizerManager() {
        return visualizerManager;
    }

    public void stop()
    {
        visualizerManager.stop();
    }

    public void updateManager()
    {
        int state;

        visualizerManager.release();
        visualizerManager = new NierVisualizerManager();

        state = visualizerManager.init(new NierVisualizerManager.NVDataSource() {

            byte[] mBuffer = new byte[512];
            byte[] recorderByteBuffer = new byte[audioRecorder.getBufferSize()/2];
            int audioLength = (int) (recorderByteBuffer.length * 1000F / audioRecorder.getSamplingRate());


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

                deviceVibrator.handleData(recorderByteBuffer);

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


        if (state == NierVisualizerManager.SUCCESS) {
            visualizerManager.start(surfaceView, new IRenderer[]{new ColumnarType1Renderer()});

        } else {
            CharSequence text = "Something went wrong with visual manager, please Stop and Start MIC";
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();

        }

    }
}
