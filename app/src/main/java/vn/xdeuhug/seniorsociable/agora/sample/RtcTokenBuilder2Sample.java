package vn.xdeuhug.seniorsociable.agora.sample;


import vn.xdeuhug.seniorsociable.agora.media.RtcTokenBuilder2;

public class RtcTokenBuilder2Sample {
    static String appId = "970CA35de60c44645bbae8a215061b33";
    static String appCertificate = "5CFd2fd1755d40ecb72977518be15d3b";
    static String channelName = "7d72365eb983485397e3e3f9d460bdda";
    static String account = "2082341273";
    static int uid = 2082341273;
    static int tokenExpirationInSeconds = 3600;
    static int privilegeExpirationInSeconds = 3600;
    static int joinChannelPrivilegeExpireInSeconds = 3600;
    static int pubAudioPrivilegeExpireInSeconds = 3600;
    static int pubVideoPrivilegeExpireInSeconds = 3600;
    static int pubDataStreamPrivilegeExpireInSeconds = 3600;

    public static void main(String[] args) {
        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        String result = token.buildTokenWithUid(appId, appCertificate, channelName, uid, RtcTokenBuilder2.Role.ROLE_SUBSCRIBER,
                tokenExpirationInSeconds, privilegeExpirationInSeconds);
        System.out.println("Token with uid: " + result);

        result = token.buildTokenWithUserAccount(appId, appCertificate, channelName, account, RtcTokenBuilder2.Role.ROLE_SUBSCRIBER,
                tokenExpirationInSeconds, privilegeExpirationInSeconds);
        System.out.println("Token with account: " + result);

        result = token.buildTokenWithUid(appId, appCertificate, channelName, uid, tokenExpirationInSeconds,
                joinChannelPrivilegeExpireInSeconds, pubAudioPrivilegeExpireInSeconds, pubVideoPrivilegeExpireInSeconds,
                pubDataStreamPrivilegeExpireInSeconds);
        System.out.println("Token with uid and privilege: " + result);

        result = token.buildTokenWithUserAccount(appId, appCertificate, channelName, account, tokenExpirationInSeconds,
                joinChannelPrivilegeExpireInSeconds, pubAudioPrivilegeExpireInSeconds,
                pubVideoPrivilegeExpireInSeconds, pubDataStreamPrivilegeExpireInSeconds);
        System.out.println("Token with account and privilege: " + result);
    }
}
