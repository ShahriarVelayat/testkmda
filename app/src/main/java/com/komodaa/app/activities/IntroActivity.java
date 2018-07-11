package com.komodaa.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.komodaa.app.App;
import com.komodaa.app.R;
import com.komodaa.app.fragments.SlideSignupFragment;
import com.komodaa.app.models.IntroSlide;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(IntroSlide.newInstance(R.layout.intro_slide_1));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide_2));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide_3));
        addSlide(new SlideSignupFragment());
        showStatusBar(false);
        setGoBackLock(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        //startActivity(new Intent(this, SignupActivity.class));
        App.getPrefs().edit().putBoolean("intro_visited", true).apply();
        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        //startActivity(new Intent(this, SignupActivity.class));

        App.getPrefs().edit().putBoolean("intro_visited", true).apply();
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    @Override public void onBackPressed() {

        super.onBackPressed();
    }
}
