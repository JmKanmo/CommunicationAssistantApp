package com.kakao.sdk.sample.kakaostory;

import android.graphics.Point;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.Root;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.web.sugar.Web;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.view.WindowManager;

import com.kakao.sdk.sample.R;

import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by kevin.kang on 16. 8. 14..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class KakaoStoryMainActivityInstrumentationTest {

    private static final int DELAY = 1000;
    private static final int LONG_DELAY = 3000;

    @Rule
    public ActivityTestRule<KakaoStoryMainActivity> activityTestRule = new ActivityTestRule<KakaoStoryMainActivity>(KakaoStoryMainActivity.class);

    @Before
    public void init() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Point[] coordinates = new Point[4];
        coordinates[0] = new Point(248, 1520);
        coordinates[1] = new Point(248, 929);
        coordinates[2] = new Point(796, 1520);
        coordinates[3] = new Point(796, 929);
        try {
            if (!uiDevice.isScreenOn()) {
                uiDevice.wakeUp();
                uiDevice.swipe(coordinates, 10);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void clean() {
        Intents.release();
        SystemClock.sleep(LONG_DELAY);
    }

    @Test
    public void testRequestProfile() {
        Espresso.onView(ViewMatchers.withId(R.id.profile_button)).perform(ViewActions.click());
        checkLogin();
        SystemClock.sleep(LONG_DELAY);
        Espresso.onView(ViewMatchers.withText("succeeded to get story profile")).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testRequestIsStoryUser() {
        Espresso.onView(ViewMatchers.withId(R.id.user_check_button)).perform(ViewActions.click());
        checkLogin();
        SystemClock.sleep(LONG_DELAY);
        Espresso.onView(ViewMatchers.withText(Matchers.startsWith("check story user"))).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testRequestPostNote() {
        Espresso.onView(ViewMatchers.withId(R.id.text_post_button)).perform(ViewActions.click());
        checkLogin();
        SystemClock.sleep(LONG_DELAY);
        Espresso.onView(ViewMatchers.withText(Matchers.startsWith("succeeded to post NOTE"))).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.delete_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
    }

    @Test
    public void testRequestPostPhoto() {
        Espresso.onView(ViewMatchers.withId(R.id.image_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
        checkLogin();
        SystemClock.sleep(LONG_DELAY);
        Espresso.onView(ViewMatchers.withText(Matchers.startsWith("succeeded to post PHOTO"))).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.delete_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
    }

    @Test
    public void testRequestPostLink() {
        Espresso.onView(ViewMatchers.withId(R.id.link_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
        checkLogin();
        Espresso.onView(ViewMatchers.withId(R.id.link_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
        SystemClock.sleep(LONG_DELAY);
        Espresso.onView(ViewMatchers.withText(Matchers.startsWith("succeeded to post NOTE"))).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.delete_post_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
    }

    public boolean checkLogin() {
        try {
            Espresso.onView(ViewMatchers.withId(R.id.button_login_with_activity)).perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.login_button_activity)).perform(ViewActions.click());
        } catch (NoMatchingViewException e) {
            return true;
        }

        try {
            Espresso.onView(ViewMatchers.withText(R.string.com_kakao_other_kakaoaccount)).perform(ViewActions.click());
        } catch (NoMatchingViewException e) {
        }

        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "email")).perform(DriverAtoms.webKeys("api01@test.kakao.com"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "password")).perform(DriverAtoms.webKeys("2014tkdtod"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "submit")).perform(DriverAtoms.webClick());

        SystemClock.sleep(3000);
        Espresso.onView(ViewMatchers.withId(R.id.kakao_story)).perform(ViewActions.click());
        return false;
    }

    static class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        protected boolean matchesSafely(Root item) {
            int type = item.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = item.getDecorView().getWindowToken();
                IBinder appToken = item.getDecorView().getApplicationWindowToken();
                if (windowToken == appToken) {
                    // windowToken == appToken means this window isn't contained by any other windows.
                    // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                    return true;
                }
            }
            return false;
        }
    }
}