package com.kakao.sdk.sample.common;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.web.sugar.Web;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.kakao.sdk.sample.MainActivity;
import com.kakao.sdk.sample.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author kevin.kang
 * Created by kevin.kang on 16. 8. 11..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SampleLoginActivityInstrumentationTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivity2Rule = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Before
    public void openLoginDialog() {
        // try logout if already logged in
        try {
            SystemClock.sleep(2000);
            Espresso.onView(ViewMatchers.withId(R.id.kakao_usermgmt)).perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.logout_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
        } catch (NoMatchingViewException e) {
        }

        // try login
//        Espresso.onView(ViewMatchers.withId(R.id.button_login_with_activity)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.login_button_activity)).perform(ViewActions.click());
        try {
            Espresso.onView(ViewMatchers.withText(R.string.com_kakao_other_kakaoaccount)).perform(ViewActions.click());
        } catch (NoMatchingViewException e) {
            // 카카오계정 로그인이 유일한 방법인 경우. (카카오톡이나 카카오스토리가 설치되어 있지 않을 때.
        }
    }

    @Test
    public void testLoginWithEmptyCredentials() {
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "submit")).perform(DriverAtoms.webClick());
        Espresso.pressBack();
    }

    @Test
    public void testLoginWithWrongCredentials() {
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "email")).perform(DriverAtoms.webKeys("api01@test.kakao.com"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "password")).perform(DriverAtoms.webKeys("2014tkdtod"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "submit")).perform(DriverAtoms.webClick());
        Espresso.pressBack();
    }

    @Test
    public void testLoginWithRightCredentials() {
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "email")).perform(DriverAtoms.webKeys("api01@test.kakao.com"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.NAME, "password")).perform(DriverAtoms.webKeys("2014tkdtod"));
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "submit")).perform(DriverAtoms.webClick());
        SystemClock.sleep(2000);
        Espresso.onView(ViewMatchers.withId(R.id.kakao_usermgmt)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.logout_button)).perform(ViewActions.scrollTo()).perform(ViewActions.click());
    }
}
