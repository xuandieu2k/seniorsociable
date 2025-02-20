package vn.xdeuhug.seniorsociable.model.entity.modelCall;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.xdeuhug.seniorsociable.agora.media.RtcTokenBuilder2;

public class GenerateTokenAgora {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String generateToken(String appId, String appCertificate, String channelName, int uid, int expirationTimeInSeconds) {
        String result = null;

        try {
            String signature = generateSignature(appId, appCertificate, channelName, uid, expirationTimeInSeconds);
            String url = String.format("https://api.agora.io/v1/token?appid=%s&channelName=%s&uid=%s&expireTimestamp=%s&signature=%s", appId, channelName, uid, expirationTimeInSeconds, signature);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, ""))
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String generateSignature(String appId, String appCertificate, String channelName, int uid, int expirationTimeInSeconds) throws NoSuchAlgorithmException, InvalidKeyException {
        long timestamp = new Date().getTime() / 1000 + expirationTimeInSeconds;

        String sign = String.format("%s%s%s%s", appId, channelName, uid, timestamp);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(appCertificate.getBytes(), "HmacSHA1"));
        byte[] signBytes = mac.doFinal(sign.getBytes());
        return bytesToHex(signBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    static int expirationTimeInSeconds = Integer.MAX_VALUE; // The time after which the token expires

    public String GenerateTokenVideoCall(String appId,String appCertificate,String channelName,int uid)
    {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        // Calculate the time expiry timestamp
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        return tokenBuilder.buildTokenWithUid(appId, appCertificate,
                channelName, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
    }
    public String GenerateTokenVoiceCall(String appId,String appCertificate,String channelName,int uid)
    {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        // Calculate the time expiry timestamp
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        return tokenBuilder.buildTokenWithUid(appId, appCertificate,
                channelName, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp);
    }
}
