package com.example.indeedgambling;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// Might be buggy cuz of deviceID
@RunWith(AndroidJUnit4.class)
public class EntrantLoginTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ---------------- Helpers ----------------

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void loginEntrant(String email, String password) {
        onView(withId(R.id.buttonLogin)).perform(click());
        sleep(1000);

        onView(withId(R.id.login_email))
                .perform(clearText(), typeText(email), closeSoftKeyboard());

        onView(withId(R.id.login_password))
                .perform(clearText(), typeText(password), closeSoftKeyboard());

        onView(withId(R.id.btn_login)).perform(click());
        sleep(2000);
    }

    // ---------------- Tests ----------------

    @Test
    public void test1_HomeDisplayed() {
        loginEntrant("jin@b.com", "123");
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    @Test
    public void test2_DeviceID() {
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }






}
