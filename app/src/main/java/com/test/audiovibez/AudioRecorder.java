package com.test.audiovibez;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorder {
    private final int RECORDER_SAMPLE_RATE = 44100;
    private final int RECORDER_CHANNELS = 1;
    private final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    private final int bufferSize = 2 * AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    private final int SAMPLING_RATE = 44100;

    private AudioRecord recorder;

    public AudioRecorder()
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT, bufferSize);
    }

    public AudioRecord getRecorder()
    {
        return recorder;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public int getSamplingRate()
    {
        return SAMPLING_RATE;
    }

    public void start()
    {
        recorder.startRecording();
    }

    public void stop()
    {
        recorder.stop();
    }
}
