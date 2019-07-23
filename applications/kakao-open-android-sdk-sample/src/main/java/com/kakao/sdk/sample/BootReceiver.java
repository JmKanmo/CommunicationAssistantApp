package com.kakao.sdk.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 배터리 교체 등으로 부팅시 앱 자동실행 시키기
        String action = intent.getAction();
        if(action.equals("android.intent.action.BOOT_COMPLETED")){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MyService.class));
            } else {
                context.startService(new Intent(context, MyService.class));
            }

        }
    }
}



