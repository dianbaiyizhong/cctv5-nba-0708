package com.nntk.nba0708.util;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class MusicUtil {

    public static void play(Activity activity, MediaPlayer mediaPlayer) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("deep_red.mp3");
        mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepare();
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

    }
}
