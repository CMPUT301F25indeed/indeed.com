package com.example.indeedgambling;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
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
public class EntrantNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }

    private void loginEntrant() {
        onView(withId(R.id.buttonLogin)).perform(click());
        sleep(800);
        onView(withId(R.id.login_email))
                .perform(clearText(), typeText("jin@b.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password))
                .perform(clearText(), typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());
        sleep(1500);
    }

    // Home loads after login
    @Test
    public void test0_HomeDisplayed() {
        loginEntrant();
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }


    // Asummes logged in via deviceID
    // Browse Events
    @Test
    public void test1_NavigateToBrowse() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(0)
                .perform(click());
        sleep(800);
        onView(withId(R.id.browse_title)).check(matches(withText("Browse Events")));
    }

    // History
    @Test
    public void test2_NavigateToHistory() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(1)
                .perform(click());
        sleep(800);
        onView(withId(R.id.entrant_history_root)).check(matches(isDisplayed()));
    }

    // Profile / Settings
    @Test
    public void test3_NavigateToProfile() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(2)
                .perform(click());
        sleep(800);
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
    }

    // Guidelines
    @Test
    public void test4_NavigateToGuidelines() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(3)
                .perform(click());
        sleep(800);
        onView(withId(R.id.guidelines_scroll)).check(matches(isDisplayed()));
    }

    // Notifications
    @Test
    public void test5_NavigateToNotifications() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(4)
                .perform(click());
        sleep(800);
        onView(withId(R.id.notifications_title)).check(matches(withText("Notifications")));
    }

    // Scan QR
    @Test
    public void test6_NavigateToScanQR() {
        onData(anything())
                .inAdapterView(withId(R.id.entrant_home_buttons))
                .atPosition(5)
                .perform(click());
        sleep(800);
    }

    // Logout
    @Test
    public void test7_LogoutNavigatesToStartup() {
        sleep(800);
        onView(withId(R.id.entrant_logout_button_home)).perform(click());
        sleep(1200);
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }
}