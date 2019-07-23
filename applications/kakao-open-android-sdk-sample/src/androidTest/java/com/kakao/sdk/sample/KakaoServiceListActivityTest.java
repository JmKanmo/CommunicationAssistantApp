package com.kakao.sdk.sample;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.kakao.sdk.sample.kakaotalk.KakaoTalkMainActivity;
import com.kakao.sdk.sample.push.PushMainActivity;
import com.kakao.sdk.sample.usermgmt.UsermgmtMainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by kevin.kang on 16. 8. 11..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class KakaoServiceListActivityTest {
    @Rule
    public ActivityTestRule<KakaoServiceListActivity> mActivityRule = new ActivityTestRule<KakaoServiceListActivity>(KakaoServiceListActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void clean() {
        Intents.release();
        SystemClock.sleep(2000);
    }

    @Test
    public void testKakaoStory() {
        //Espresso.onView(ViewMatchers.withId(R.id.kakao_story)).perform(ViewActions.click());
        //Intents.intended(IntentMatchers.hasComponent(KakaoStoryMainActivity.class.getName()));
    }

    @Test
    public void testKakaoTalk() {
        Espresso.onView(ViewMatchers.withId(R.id.kakao_talk)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(KakaoTalkMainActivity.class.getName()));
    }

    @Test
    public void testKakaoPush() {
        Espresso.onView(ViewMatchers.withId(R.id.kakao_push)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(PushMainActivity.class.getName()));
    }

    @Test
    public void testKakaoUserMgmt() {
        Espresso.onView(ViewMatchers.withId(R.id.kakao_usermgmt)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(UsermgmtMainActivity.class.getName()));
    }
}
