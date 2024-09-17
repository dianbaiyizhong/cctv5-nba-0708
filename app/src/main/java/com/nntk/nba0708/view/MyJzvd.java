package com.nntk.nba0708.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import cn.jzvd.JzvdStd;

public class MyJzvd extends JzvdStd {

    public void setEnd(boolean end) {
        isEnd = end;
    }

    private boolean isEnd = false;

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    private Adapter adapter;

    public interface Adapter {
        public void completion();
    }


    public MyJzvd(Context context) {
        super(context);
    }

    public MyJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        bottomProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        mediaInterface.setVolume(0f, 0f);

    }


}
