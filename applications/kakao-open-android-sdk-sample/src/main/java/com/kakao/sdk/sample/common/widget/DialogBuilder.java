package com.kakao.sdk.sample.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kakao.sdk.sample.R;

/**
 * @author leo.shin
 * Created by leoshin on 15. 6. 19..
 */
public class DialogBuilder {

    private static class CustomDialog extends Dialog {
        private final DialogBuilder builder;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            Log.d("프로세스","DialogBuilder실행");
            super.onCreate(savedInstanceState);

            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWindow);

            setContentView(R.layout.view_popup);
            initView();
        }

        public CustomDialog(Context context, DialogBuilder builder) {
            // Dialog 배경을 투명 처리 해준다.
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            this.builder = builder;
        }

        @SuppressWarnings("deprecation")
        private void initView() {
            final String title = builder.title;
            final String message = builder.message;
            final String negativeBtnText = builder.negativeBtnText;
            final String positiveBtnText = builder.positiveBtnText;
            final OnClickListener positiveListener = builder.positiveListener;
            final OnClickListener negativeListner = builder.negativeListner;
            final View contentView = builder.contentView;
            final boolean showTitleDivider = builder.showTitleDivider;
            final int titleBgResId = builder.titleBgResId;
            final int titleTextColor = builder.titleTextColor;

            TextView titleView = (TextView) findViewById(R.id.title);
            if (title != null && title.length() > 0) {
                titleView.setText(title);
            } else {
                findViewById(R.id.popup_header).setVisibility(View.GONE);
            }

            if (titleBgResId > 0) {
                titleView.setBackgroundResource(titleBgResId);
            }

            if (titleTextColor > 0) {
                titleView.setTextColor(titleView.getContext().getResources().getColor(titleTextColor));
            }

            ImageView titleDivider = (ImageView) findViewById(R.id.divide);
            if (showTitleDivider) {
                titleDivider.setVisibility(View.VISIBLE);
            } else {
                titleDivider.setVisibility(View.GONE);
            }

            TextView messageView = (TextView) findViewById(R.id.content);
            if (message != null && message.length() > 0) {
                messageView.setText(message);
                messageView.setMovementMethod(new ScrollingMovementMethod());
            } else {
                messageView.setVisibility(View.GONE);
            }

            if (contentView != null) {
                FrameLayout container = (FrameLayout)findViewById(R.id.content_group);
                container.setVisibility(View.VISIBLE);
                container.addView(contentView);
            }

            Button positiveBtn = (Button) findViewById(R.id.bt_right);
            if (positiveBtnText != null && positiveBtnText.length() > 0) {
                positiveBtn.setText(positiveBtnText);
            } else {
                positiveBtn.setVisibility(View.GONE);
            }

            Button negativeBtn = (Button) findViewById(R.id.bt_left);
            if (negativeBtnText != null && negativeBtnText.length() > 0) {
                negativeBtn.setText(negativeBtnText);
            } else {
                negativeBtn.setVisibility(View.GONE);
            }

            if (positiveBtn.getVisibility() == View.VISIBLE && negativeBtn.getVisibility() == View.GONE) {
                positiveBtn.setBackgroundResource(R.drawable.popup_btn_c);
            }

            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeListner != null) {
                        negativeListner.onClick(CustomDialog.this, 0);
                    }
                    dismiss();
                }
            });

            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (positiveListener != null) {
                        positiveListener.onClick(CustomDialog.this, 0);
                    }
                    dismiss();
                }
            });

            findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            findViewById(R.id.popup).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // skip
                }
            });
        }
    }

    private Context context = null;
    private String title = null;
    private String message = null;
    private String positiveBtnText = null;
    private String negativeBtnText = null;
    private View contentView = null;
    private int titleBgResId = 0;
    private int titleTextColor = 0;
    private boolean showTitleDivider = true;
    private DialogInterface.OnClickListener positiveListener = null;
    private DialogInterface.OnClickListener negativeListner = null;

    public DialogBuilder(Context context) {
        this.context = context;
    }

    public DialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogBuilder setTitle(int titleResId) {
        this.title = context.getString(titleResId);
        return this;
    }

    public DialogBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public DialogBuilder setMessage(int messageResId) {
        this.message = context.getString(messageResId);
        return this;
    }

    public DialogBuilder setPositiveButton(int positiveResId, DialogInterface.OnClickListener positiveListener) {
        this.positiveBtnText = context.getString(positiveResId);
        this.positiveListener = positiveListener;
        return this;
    }

    public DialogBuilder setNegativeButton(int negativeResId, DialogInterface.OnClickListener negativeListner) {
        this.negativeBtnText = context.getString(negativeResId);
        this.negativeListner = negativeListner;
        return this;
    }

    public DialogBuilder setView(View view) {
        this.contentView = view;
        return this;
    }

    public DialogBuilder setTitleBgResId(int titleBgResId) {
        this.titleBgResId = titleBgResId;
        return this;
    }

    public DialogBuilder setShowTitleDivider(boolean showTitleDivider) {
        this.showTitleDivider = showTitleDivider;
        return this;
    }

    public DialogBuilder setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
        return this;
    }

    public Dialog create() {
        return new CustomDialog(context, this);
    }
}
