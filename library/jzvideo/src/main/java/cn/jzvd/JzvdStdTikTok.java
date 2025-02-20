package cn.jzvd;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import cn.jzvd.JzvdStd;
import cn.jzvd.R;

public class JzvdStdTikTok extends JzvdStd {
    public JzvdStdTikTok(Context context) {
        super(context);
    }

    public JzvdStdTikTok(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        bottomContainer.setVisibility(GONE);
        topContainer.setVisibility(GONE);
        bottomProgressBar.setVisibility(VISIBLE);
        posterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }


    //changeUiTo 真能能修改ui的方法
    @Override
    public void changeUiToNormal() {
        super.changeUiToNormal();
        bottomContainer.setVisibility(GONE);
        topContainer.setVisibility(GONE);
    }

    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro,
                                        int posterImg, int bottomPro, int retryLayout) {
        topContainer.setVisibility(INVISIBLE);
        bottomContainer.setVisibility(INVISIBLE);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        posterImageView.setVisibility(posterImg);
        bottomProgressBar.setVisibility(VISIBLE);
        mRetryLayout.setVisibility(retryLayout);
    }

    @Override
    public void dissmissControlView() {
        if (state != STATE_NORMAL
                && state != STATE_ERROR
                && state != STATE_AUTO_COMPLETE) {
            post(() -> {
                bottomContainer.setVisibility(View.INVISIBLE);
                topContainer.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);
                if (clarityPopWindow != null) {
                    clarityPopWindow.dismiss();
                }
                if (screen != SCREEN_TINY) {
                    bottomProgressBar.setVisibility(View.VISIBLE    );
                }
            });
        }
    }


    @Override
    public void onClickUiToggle() {
        super.onClickUiToggle();
        Log.i(TAG, "click blank");
        startButton.performClick();
        bottomContainer.setVisibility(GONE);
        topContainer.setVisibility(GONE);
    }

    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(INVISIBLE);
            startButton.setImageResource(R.drawable.jz_play_normal);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_play_normal);
            replayTextView.setVisibility(VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.jz_play_normal);
            replayTextView.setVisibility(GONE);
        }
    }
}
