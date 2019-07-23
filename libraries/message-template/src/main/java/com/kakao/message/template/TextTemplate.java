package com.kakao.message.template;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines parameters for building text messages.
 * @author kevin.kang. Created on 2017. 9. 12..
 */

public class TextTemplate implements TemplateParams {
    private final String text;
    private final LinkObject linkObject;
    private final String buttonTitle;
    private final List<ButtonObject> buttons;

    public TextTemplate(final Builder builder) {
        text = builder.text;
        linkObject = builder.link;
        buttonTitle = builder.buttonTitle;
        buttons = builder.buttons;
    }

    public static Builder newBuilder(final String text, final LinkObject linkObject) {
        return new Builder(text, linkObject);
    }


    @Override
    public String getObjectType() {
        return MessageTemplateProtocol.TYPE_TEXT;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(MessageTemplateProtocol.OBJ_TYPE, getObjectType());
            jsonObject.put(MessageTemplateProtocol.TEXT, text);
            jsonObject.put(MessageTemplateProtocol.LINK, linkObject.toJSONObject());
            jsonObject.put(MessageTemplateProtocol.BUTTON_TITLE, buttonTitle);

            if (buttons != null) {
                JSONArray buttonArray = new JSONArray();
                for (ButtonObject button : buttons) {
                    buttonArray.put(button.toJSONObject());
                }
                jsonObject.put(MessageTemplateProtocol.BUTTONS, buttonArray);
            }
        } catch (JSONException e) {
            Log.w("com.kakao.message", e.toString());
        }
        return jsonObject;
    }

    public List<ButtonObject> getButtons() {
        return buttons;
    }

    public LinkObject getLinkObject() {
        return linkObject;
    }

    public String getText() {
        return text;
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    /**
     * Class for building text template.
     */
    public static class Builder {
        String text;
        LinkObject link;
        String buttonTitle;
        List<ButtonObject> buttons;

        public Builder(final String text, final LinkObject linkObject) {
            if (text == null) {
                throw new IllegalArgumentException("TextTemplate's text field cannot be null.");
            }
            if (linkObject == null) {
                throw new IllegalArgumentException("TextTemplate's link field cannot be null.");
            }
            this.text = text;
            this.link = linkObject;
            buttons = new ArrayList<>();
        }

        public Builder setButtonTitle(final String buttonTitle) {
            this.buttonTitle = buttonTitle;
            return this;
        }

        public Builder addButton(final ButtonObject button) {
            if (button != null) {
                buttons.add(button);
            }
            return this;
        }

        public TextTemplate build() {
            return new TextTemplate(this);
        }
    }
}
