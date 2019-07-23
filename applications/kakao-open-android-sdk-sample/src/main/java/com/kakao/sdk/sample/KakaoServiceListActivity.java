package com.kakao.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.kakaotalk.KakaoTalkMainActivity;
import com.kakao.sdk.sample.push.PushMainActivity;
import com.kakao.sdk.sample.usermgmt.UsermgmtMainActivity;

//import com.kakao.sdk.sample.kakaostory.KakaoStoryMainActivity;

public class KakaoServiceListActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_service_list);

        Log.d("프로세스","카카오톡서비스리스트액티비티실행");

        findViewById(R.id.kakao_story).setOnClickListener(this);
        findViewById(R.id.kakao_talk).setOnClickListener(this);
        findViewById(R.id.kakao_push).setOnClickListener(this);
        findViewById(R.id.kakao_usermgmt).setOnClickListener(this);
        findViewById(R.id.title_back).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.kakao_story:
                KakaoToast.makeToast(getApplicationContext(), "중지된 서비스입니다.", Toast.LENGTH_LONG).show();
                break;
            case R.id.kakao_talk:
                startActivity(new Intent(this, KakaoTalkMainActivity.class));
                break;
            case R.id.kakao_push:
                startActivity(new Intent(this, PushMainActivity.class));
                break;
            case R.id.kakao_usermgmt:
                startActivity(new Intent(this, UsermgmtMainActivity.class));
                break;
            default:
                break;
        }
    }
}
