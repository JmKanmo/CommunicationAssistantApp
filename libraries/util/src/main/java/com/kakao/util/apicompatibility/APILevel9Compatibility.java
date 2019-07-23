package com.kakao.util.apicompatibility;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

import com.kakao.util.helper.log.Logger;

/**
 * @author leoshin on 15. 11. 4.
 */
@TargetApi(VERSION_CODES.GINGERBREAD)
class APILevel9Compatibility extends APICompatibility {
    @Override
    public String getSmsMessage(Intent intent) {
        String smsDisplayMessage = null;
        Bundle bundle = intent.getExtras();
        Object[] data = (Object[]) bundle.get("pdus");
        for (Object pdu : data) {
            Logger.d("legacy SMS implementation (before KitKat)");
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            if (message == null) {
                Logger.e("SMS message is null -- ABORT");
                break;
            }
            // see getMessageBody();
            smsDisplayMessage = message.getDisplayMessageBody();
        }

        return smsDisplayMessage;
    }
}
