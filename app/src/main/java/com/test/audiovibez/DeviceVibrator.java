package com.test.audiovibez;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class DeviceVibrator {
    Vibrator vibrator;
    Context context;
    boolean amplify;

    public DeviceVibrator(Context context) {

        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.amplify = false;

    }

    public void setAmplify(boolean amplify)
    {
        this.amplify = amplify;
    }

    public void handleData(byte[] audioData)
    {
        int RMS = computeRMS(audioData);
        int mapped = mappedValue(RMS);


        long[] time = new long[]{50};
        int[] pattern = new int[]{mapped};
        if(mapped == 0)
            vibrator.cancel();
        else
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
        value = (value < 20)? 0: value; // noise cancellation

        if(amplify)
            value *= 1.5;

        value = (value > max)? max: value;


        return (int) value;
    }
}
