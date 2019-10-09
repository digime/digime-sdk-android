/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.digi.examples.ca_no_sdk.R;

public abstract class LoadingActivity extends BaseActivity {

    private RelativeLayout loadingViews;
    private ProgressBar loadingProgressBar;
    private TextView loadingMessage;

    private AnimatorSet mainViewHideAnimator;
    private AnimatorSet mainViewShowAnimator;
    private Animation textFadeIn;
    private Animation textFadeOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingViews = (RelativeLayout) findViewById(R.id.loading_views);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);
        loadingMessage = (TextView) findViewById(R.id.loading_message);

        setUpAnimations();
    }

    private void setUpAnimations() {
        // show loading animators
        ObjectAnimator progressBarShowAnimator = ObjectAnimator.ofFloat(loadingProgressBar, "alpha", 1f);
        ObjectAnimator progressBarMessageShowAnimator = ObjectAnimator.ofFloat(loadingMessage, "alpha", 1f);
        mainViewShowAnimator = new AnimatorSet();
        mainViewShowAnimator.play(progressBarShowAnimator).with(progressBarMessageShowAnimator);
        mainViewShowAnimator.setDuration(500);

        ObjectAnimator progressBarHideAnimator = ObjectAnimator.ofFloat(loadingProgressBar, "alpha", 0f);
        ObjectAnimator progressBarMessageHideAnimator = ObjectAnimator.ofFloat(loadingMessage, "alpha", 0f);
        mainViewHideAnimator = new AnimatorSet();
        mainViewHideAnimator.play(progressBarHideAnimator).with(progressBarMessageHideAnimator);
        mainViewHideAnimator.setDuration(500);

        textFadeIn = new AlphaAnimation(0.0f, 1.0f);
        textFadeIn.setDuration(300);
        textFadeOut = new AlphaAnimation(1.0f, 0.0f);
        textFadeOut.setDuration(300);
    }

    private void clearAnimators() {
        mainViewShowAnimator.removeAllListeners();
        mainViewHideAnimator.removeAllListeners();
        mainViewShowAnimator.end();
        mainViewHideAnimator.end();
    }

    private void setVisibilityOfLoadingViews(boolean visible) {
        loadingViews.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void contentLoading(@NonNull final String message) {
        if (loadingViews.getVisibility() == View.VISIBLE) {
            loadingMessage.startAnimation(textFadeOut);
            textFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    loadingMessage.setText(message);
                    loadingMessage.startAnimation(textFadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            clearAnimators();
            loadingMessage.setAlpha(0);
            loadingMessage.setText(message);
            setVisibilityOfLoadingViews(true);
            mainViewShowAnimator.start();
        }
    }

    protected void contentFinishedLoading() {
        clearAnimators();
        setVisibilityOfLoadingViews(true);
        mainViewHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibilityOfLoadingViews(false);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mainViewHideAnimator.start();
    }
}
