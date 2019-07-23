package com.kakao.auth.network;

import com.kakao.network.IRequest;
import com.kakao.common.IConfiguration;
import com.kakao.common.PhaseInfo;

/**
 * @author kevin.kang. Created on 2017. 11. 30..
 */

public interface AuthorizedRequest extends IRequest {
    void setAccessToken(final String accessToken);
    void setConfiguration(final PhaseInfo phaseInfo, final IConfiguration configuration);
}
