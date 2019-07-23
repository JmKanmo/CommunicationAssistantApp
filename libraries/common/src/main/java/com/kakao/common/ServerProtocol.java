package com.kakao.common;

/**
 * @author kevin.kang. Created on 2018. 7. 4..
 */
public final class ServerProtocol {
    @Deprecated
    public static final String PLUS_FRIEND_AUTHORITY = initPlusFriendAuthority();
    public static final String PF_ADD_PATH = "friend";
    public static final String PF_CHAT_PATH = "chat";

    /**
     * 카카오내비 앱을 통해 실행 가능한 기능들.
     */
    public static final String NAVI_SHARE_PATH = "sharePoi";
    public static final String NAVI_GUIDE_PATH = "navigate";

    public static final String SCHEME = "https";

    private static String initPlusFriendAuthority() {
        return "pf.kakao.com";
    }

    public static String plusFriendAuthority() {
        PhaseInfo phaseInfo = KakaoContextService.getInstance().phaseInfo();
        if (phaseInfo != null) {
            switch (phaseInfo.phase()) {
                case DEV:
                case SANDBOX:
                    return "sandbox-pf.kakao.com";
                case CBT:
                    return "beta-pf.kakao.com";
                case PRODUCTION:
                default:
                    return "pf.kakao.com";
            }
        }
        return initPlusFriendAuthority();
    }

    public static String naviAuthority() {
        PhaseInfo phaseInfo = KakaoContextService.getInstance().phaseInfo();
        if (phaseInfo != null) {
            switch (phaseInfo.phase()) {
                case DEV:
                case SANDBOX:
                    return "sandbox-kakaonavi-wguide.kakao.com";
                case CBT:
                case PRODUCTION:
                default:
                    return "kakaonavi-wguide.kakao.com";
            }
        }
        return "kakaonavi-wguide.kakao.com";
    }
}
