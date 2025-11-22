package com.example.indeedgambling;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class NotificationIntentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNotificationFlow_EventToNotificationForm() {
        // 1. Login as organizer (you'll need to set this up)
        // 2. Navigate to upcoming events
        // 3. Click an event to open popup
        onView(withId(R.id.Organizer_UpcomingEventList)).perform(click());

        // 4. Verify notification button exists in popup
        onView(withId(R.id.btnSendNotifications)).check(matches(isDisplayed()));

        // 5. Click notification button
        onView(withId(R.id.btnSendNotifications)).perform(click());

        // 6. Verify notification form opens
        onView(withId(R.id.notificationSenderFragment)).check(matches(isDisplayed()));

        // 7. Verify event ID is auto-filled
        onView(withId(R.id.edit_event_id))
                .check(matches(withText(not(isEmptyString()))))
                .check(matches(not(isEnabled())));
    }

    @Test
    public void testSendNotification_ValidInput() {
        // Navigate to notification form (setup steps above)

        // Fill notification form
        onView(withId(R.id.edit_notification_title)).perform(typeText("Test Event Update"));
        onView(withId(R.id.edit_notification_message)).perform(typeText("This is a test notification"));

        // Select recipient type
        onView(withId(R.id.radio_waiting_list)).perform(click());

        // Click send
        onView(withId(R.id.button_send_notification)).perform(click());

        // Verify success (toast or navigation back)
        // This depends on your success feedback
    }
}