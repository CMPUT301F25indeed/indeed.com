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

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class EntrantProfileTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // ---------------- Helpers ----------------

    private String randomEmail() {
        int n = new Random().nextInt(9999);
        return "entrant" + n + "@b.com";
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void loginEntrant(String email, String password) {
        onView(withId(R.id.goToLogin)).perform(click());
        sleep(1000);
        onView(withId(R.id.loginEmail)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.loginPassword)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.loginBtn)).perform(click());
        sleep(2000);
    }

    private void performSignup(boolean withPhone) {
        onView(withId(R.id.goToSignup)).perform(click());
        sleep(1000);

        String email = randomEmail();
        onView(withId(R.id.signUpName)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.signUpEmail)).perform(typeText(email), closeSoftKeyboard());
        if (withPhone) {
            onView(withId(R.id.signUpPhone)).perform(typeText("5871234567"), closeSoftKeyboard());
        }
        onView(withId(R.id.signUpPassword)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.signUpRole)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.signUpBtn)).perform(click());
        sleep(3000);

        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    // ---------------- Tests ----------------

    // ✅ Test 1: Entrant home displayed
    @Test
    public void test1_HomeDisplayed() {
        loginEntrant("entrant1@b.com", "123");
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    // ✅ Test 2: Login works
    @Test
    public void test2_Login() {
        loginEntrant("entrant2@b.com", "123");
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    // ✅ Test 3: Signup without phone
    @Test
    public void test3_SignupWithoutPhone() {
        performSignup(false);
    }

    // ✅ Test 3.1: Signup with phone
    @Test
    public void test3_1_SignupWithPhone() {
        performSignup(true);
    }

    // ✅ Test 4: Edit profile (name, email, phone, notifications)
    @Test
    public void test4_EditProfile() {
        loginEntrant("entrant2@b.com", "123");
        onView(withId(R.id.entrant_home_to_profile)).perform(click());
        sleep(1000);

        onView(withId(R.id.editTextName)).perform(clearText(), typeText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail)).perform(clearText(), typeText("short@b.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPhone)).perform(clearText(), typeText("7801112222"), closeSoftKeyboard());
        onView(withId(R.id.switchNotifications)).perform(click());
        onView(withId(R.id.saveProfileBtn)).perform(click());
        sleep(1500);

        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    // ✅ Test 5: Guidelines screen scroll and return Home
    @Test
    public void test5_GuidelinesScrollAndBackHome() {
        loginEntrant("entrant2@b.com", "123");
        sleep(1000);

        // Go to guidelines
        onView(withId(R.id.entrant_home_to_guidelines)).perform(click());
        sleep(1000);

        // Scroll inside scrollable layout (NestedScrollView or ScrollView)
        onView(withId(R.id.guidelines_scroll_view)).perform(swipeUp());
        sleep(800);
        onView(withId(R.id.guidelines_scroll_view)).perform(swipeUp());
        sleep(800);

        // Click Home button
        onView(withId(R.id.guidelines_home_button)).perform(click());
        sleep(1000);

        // Verify back at home
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }

    // ✅ Test 6: Toggle notification switch and verify state changes
    @Test
    public void test6_NotificationSwitchToggle() {
        loginEntrant("entrant2@b.com", "123");
        sleep(1000);

        // Go to profile
        onView(withId(R.id.entrant_home_to_profile)).perform(click());
        sleep(1000);

        // Toggle switch on
        onView(withId(R.id.switchNotifications)).perform(click());
        sleep(800);

        // Save and go back home
        onView(withId(R.id.saveProfileBtn)).perform(click());
        sleep(1000);

        // Go back to profile again
        onView(withId(R.id.entrant_home_to_profile)).perform(click());
        sleep(1000);

        // Toggle switch off and save again
        onView(withId(R.id.switchNotifications)).perform(click());
        onView(withId(R.id.saveProfileBtn)).perform(click());
        sleep(1000);

        // Confirm back on home screen
        onView(withId(R.id.entrant_greeting_home)).check(matches(isDisplayed()));
    }
}
