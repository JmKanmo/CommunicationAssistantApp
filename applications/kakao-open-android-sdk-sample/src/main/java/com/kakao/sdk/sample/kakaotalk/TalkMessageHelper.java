package com.kakao.sdk.sample.kakaotalk;

import android.content.Context;
import android.content.DialogInterface;

import com.kakao.sdk.sample.BuildConfig;
import com.kakao.sdk.sample.R;
import com.kakao.sdk.sample.common.widget.DialogBuilder;
import com.kakao.sdk.sample.FriendsMainActivity.MSG_TYPE;

/**
 * @author leoshin on 15. 9. 4.
 */
public class TalkMessageHelper {
    public static String getMemoTemplateId() {
        switch (BuildConfig.FLAVOR) {
            case "dev":
                return "20253";
            case "sandbox":
                return "224";
            case "cbt":
            case "production":
                return "3356";
            default:
                return null;
        }
    }

    public static String getSampleTemplateId(MSG_TYPE msgType) {
        switch (BuildConfig.FLAVOR) {
            case "dev":
                return getAlphaTemplateId(msgType);
            case "sandbox":
                return getSandboxTemplateId(msgType);
            case "cbt":
            case "production":
                return getReleaseTemplateId(msgType);
            default:
                return null;
        }
    }

    public static String getAlphaTemplateId(MSG_TYPE msgType) {
        switch (msgType) {
            case FEED:
                return "20253";
            case LIST:
                return "20254";
            default:
                return "20253";
        }
    }

    public static String getSandboxTemplateId(MSG_TYPE msgType) {
        switch (msgType) {
            case FEED:
                return "224";
            case LIST:
                return "225";
            default:
                return "224";
        }
    }

    public static String getReleaseTemplateId(MSG_TYPE msgType) {
        switch (msgType) {
            case FEED:
                return "3356";
            case LIST:
                return "3357";
            default:
                return "3356";
        }
    }

    public static void showSendMessageDialog(Context context, final DialogInterface.OnClickListener listener) {
        final String message = context.getString(R.string.send_message);
        new DialogBuilder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (listener != null) {
                        listener.onClick(dialog, which);
                    }
                    dialog.dismiss();
                }).create().show();
    }
}
