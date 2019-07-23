package com.kakao.util.apicompatibility;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.kakao.util.helper.log.Logger;

/**
 * @author leoshin on 15. 11. 23.
 */
@TargetApi(VERSION_CODES.KITKAT)
class APILevel19Compatibility extends APILevel9Compatibility {

    @Override
    public String getSmsMessage(Intent intent) {
        String smsDisplayMessage = null;
        for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            Logger.d("KitKat or newer");
            if (message == null) {
                Logger.e("SMS message is null -- ABORT");
                break;
            }
            //see getMessageBody();
            smsDisplayMessage = message.getDisplayMessageBody();
        }

        return smsDisplayMessage;
    }
}
