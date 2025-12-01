package com.example.indeedgambling;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class CheckStartupTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void startup_displaysLoginButton() {
        onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()));
    }

    @Test
    public void startup_displaysSignupButton() {
        onView(withId(R.id.goToSignup))
                .check(matches(isDisplayed()));
    }

    @Test
    public void startup_displaysSubtitle(){
        onView(withId(R.id.app_subtitle))
                .check(matches(isDisplayed()));
    }

    @Test
    public void startup_displaysLogo(){
        onView(withId(R.id.logo))
                .check(matches(isDisplayed()));
    }

    @Test
    public void startup_displaysTitle(){
        onView(withId(R.id.app_title))
                .check(matches(isDisplayed()));
    }

}