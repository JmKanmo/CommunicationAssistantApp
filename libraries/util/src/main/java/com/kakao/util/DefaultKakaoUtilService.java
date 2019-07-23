/*
  Copyright 2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.kakao.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kevin.kang. Created on 2017. 5. 30..
 */

class DefaultKakaoUtilService implements KakaoUtilService {
    @Override
    public Intent resolveIntent(Context context, Intent intent, int minVersion) {
        // Invalid argument
        if (intent == null) {
            return null;
        }

        // 해당 intent를 지원하는 kakaotalk query.
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);

        // No KakaoTalk app supporting this intent is installed on this device. Just return null.
        if (infos.size() == 0) {
            return null;
        }

        List<Intent> targetIntents = new ArrayList<>();
        List<LabeledIntent> labeledIntents = new ArrayList<>();

        for (ResolveInfo info : infos) {
            if (validateTalkAuthenticity(context, info.activityInfo.applicationInfo.packageName, minVersion)) {
                Intent clone = (Intent) intent.clone();
                clone.setComponent(new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
                targetIntents.add(clone);
                labeledIntents.add(new LabeledIntent(intent, info.activityInfo.applicationInfo.packageName, info.activityInfo.applicationInfo.labelRes, info.activityInfo.applicationInfo.icon));
            }
        }

        // No talk/story app with valid signature is installed.
        if (targetIntents.size() == 0) {
            return null;
        }

        if (targetIntents.size() == 1) {
            return targetIntents.get(0);
        }

        // More than one talk/story app is installed.
        Intent chooserIntent = Intent.createChooser(labeledIntents.remove(0), context.getString(R.string.com_kakao_talk_chooser_text));
        if (labeledIntents.size() > 0) {
            Parcelable[] labeledIntentsParcelable = labeledIntents.toArray(new Parcelable[labeledIntents.size()]);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, labeledIntentsParcelable);
        }
        return chooserIntent;
    }

    private boolean validateTalkMinVersion(final PackageInfo packageInfo, final int minVersion) {
        return packageInfo.versionCode >= minVersion;
    }

    private boolean validateTalkSignature(final PackageInfo packageInfo) {
        for (Signature signature : packageInfo.signatures) {
            String signatureCharsString = signature.toCharsString();
            if (ALLOWED_SIGNATURES.contains(signatureCharsString)) {
                return true;
            }
        }
        return false;
    }

    private boolean validateTalkPackageName(String packageName) {
        return ALLOWED_PACKAGES.contains(packageName);
    }

    @SuppressLint("PackageManagerGetSignatures")
    private boolean validateTalkAuthenticity(final Context context, final String packageName, final int minVersion) {
        PackageInfo talkPackageInfo;
        try {
            talkPackageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
            return validateTalkMinVersion(talkPackageInfo, minVersion) && validateTalkPackageName(packageName) && validateTalkSignature(talkPackageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static final String TALK_PACKAGE_NAME = "com.kakao.talk";
    private static final String ONE_TALK_PACKAGE_NAME = "com.kakao.onetalk";
    private static final String STORY_PACKAGE_NAME = "com.kakao.story";

    private static final String RELEASE_SIGNATURE = "308201db30820144a00302010202044c707197300d06092a864886f70d010105050030" +
            "31310b3009060355040613026b6f310e300c060355040a13056b616b616f31123010060355040b13096b616b616f7465616d3020170d" +
            "3130303832323030333834375a180f32313130303732393030333834375a3031310b3009060355040613026b6f310e300c060355040a" +
            "13056b616b616f31123010060355040b13096b616b616f7465616d30819f300d06092a864886f70d010101050003818d003081890281" +
            "8100aef387bc86e022a87e66b8c42153284f18e0c468cf9c87a241b989729dfdad3dd9e1847546d01a2819ba77f3974a47b473c926ac" +
            "ae173fd90c7e635000721feeef6705da7ae949a35b82900a0f67d9464d73ed8a98c37f4ac70729494a17469bc40d4ee06d043b09147e" +
            "badc55fa1020968d7036c5fb9b8c148cba1d8e9d9fc10203010001300d06092a864886f70d0101050500038181005569be704c68cff6" +
            "221c1e04dd8a131110f9f5cd2138042286337fd6014a1b1d2d3eeb266ae1630afe56bf63c07dd0b5c8fad46dcb9f802f9a7802fb89eb" +
            "3b4777b9665bb1ed9feaf1dc7cac4f91abedfc81187ff6d2f471dbd12335d2c0ef0e2ee719df6e763f814b9ac91f8be37fd11d406867" +
            "00d66be6de22a1836f060f01";

    private static final String DEBUG_SIGNATURE = "308201e53082014ea00302010202044f4ae542300d06092a864886f70d01010505003037" +
            "310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f69642044656275" +
            "67301e170d3132303232373032303635385a170d3432303231393032303635385a3037310b30090603550406130255533110300e0603" +
            "55040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730819f300d06092a864886f70d0101010500" +
            "03818d0030818902818100c0b41c25ef21a39a13ce89c82dc3a14bf9ef0c3094aa2ac1bf755c9699535e79119e8b980c0ecdcc51f259" +
            "eb0d8b2077d41de8fcfdeaac3f386c05e2a684ecb5504b660ad7d5a01cce35899f96bcbd099c9dcb274c6eb41fef861616a12fb45bc5" +
            "7a19683a8a97ab1a33d9c70128878b67dd1b3a388ad5121d1d66ff04c065ff0203010001300d06092a864886f70d0101050500038181" +
            "000418a7dacb6d13eb61c8270fe1fdd006eb66d0ff9f58f475defd8dc1fb11c41e34ce924531d1fd8ad26d9479d64f54851bf57b8dfe" +
            "3a5d6f0a01dcad5b8c36ac4ac48caeff37888c36483c26b09aaa9689dbb896938d5afe40135bf7d9f12643046301867165d28be0baa3" +
            "513a5084e182f7f9c044d5baa58bdce55fa1845241";

    private static final String ONE_RELEASE_SIGNATURE = "30820303308201eba003020102020452441f49300d06092a864886f70d01010b0" +
            "5003031310b3009060355040613026b6f310e300c060355040a13056b616b616f31123010060355040b13096b616b616f7465616d302" +
            "0170d3137303631393039353135315a180f33303135313032313039353135315a3031310b3009060355040613026b6f310e300c06035" +
            "5040a13056b616b616f31123010060355040b13096b616b616f7465616d30820122300d06092a864886f70d01010105000382010f003" +
            "082010a0282010100c2867a4e6fb76eaa00d5ecac63c897ebd924bb40d3f7dd90f73599a2049ae40abc4c7b1dce10dafbfdabbebf365" +
            "3d7c6a18a3ade469dbe5bd0590748aae4151491001eadd8b02f7469646530595c028ed70feeacd7184fc5b0fd0ceb95addd03b7d1809" +
            "7a32a4afc830e209e25c65656587d891282c610429965cc44f3d63ea249d4df41453ac30ca1b3eaf4b1f8fc5cf41af4964f66f611b79" +
            "9f6246fcb1d6b42fff8cff202a433a90ccd25385c4d015ac770dedf8914d86c53b0eebdd4c5c5e3a509e360785fc38ee075b6d7faad1" +
            "9f7c876ff3949a85f601158f99c67ee14c20ff759d3057dc258146f579a5e3d90457d9996f004808f4aa625ab9a67dfc30203010001a" +
            "321301f301d0603551d0e041604141487897f76c0e76161888c86336325b6e58fce5d300d06092a864886f70d01010b0500038201010" +
            "07bf867fa1b4ef0ea4d6de127238319c84dcae79398e60f960ab71a8bdf488b0aa07888e994bba531f4419037cd006b7d9a64860a659" +
            "1b96534967444b8854bef6a6eff3161dbcbebfe5e6c979650c9d51f76676b217b8285992f4a172d4a857775c42dc3875796434b13b78" +
            "d6cfb174bfaa0c59976fb7a1cd4d26527881cfd39a61cd35843dd2cd49afd7d3966947b2662fc44dbff3704094687ce70ccabeb8a9d9" +
            "3f39974bd11fdb1dcb9404d8a6408cae45c315ced013f088c5264ce9c8738715ecf83bc991d4e3971e4a2cc39bcd11be426d79363898" +
            "1455d083cfd7bfd3b88ecd11e581260ae7fbf27b8c9cdf0da49a467e375f4273d6e01d7114ac7126f";

    private static final List<String> ALLOWED_SIGNATURES = Arrays.asList(RELEASE_SIGNATURE, DEBUG_SIGNATURE, ONE_RELEASE_SIGNATURE);

    private static final List<String> ALLOWED_PACKAGES = Arrays.asList(TALK_PACKAGE_NAME, ONE_TALK_PACKAGE_NAME, STORY_PACKAGE_NAME);
}

