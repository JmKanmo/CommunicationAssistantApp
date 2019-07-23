package com.kakao.sdk.sample;


import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

public class VolumeObserver extends ContentObserver{
    Context context;

    public VolumeObserver(Context c, Handler handler) {
        super(handler);
        context=c;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        ChatActivity_mode1.volume_val = currentVolume;
    }
}