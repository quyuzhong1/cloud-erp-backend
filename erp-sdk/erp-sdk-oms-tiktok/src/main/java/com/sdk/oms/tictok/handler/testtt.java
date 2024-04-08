package com.sdk.oms.tictok.handler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class testtt {
    public static void main(String[] args) {
        // Replace secret with your app
        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";

        // Get timestamp
        long ts = new Date().getTime() / 1000;

        // Set timestamp variable
        System.out.println("timestamp: " + ts);

        // Calculate signature
        String signature = calSign(secret, ts);

        // Set sign variable
        System.out.println("sign: " + signature);

        // Construct request URL
        String requestUrl = "https://open-api.tiktokglobalshop.com/authorization/202309/shops";
        requestUrl += "?app_key=6buinkjt3hmld";
        requestUrl += "&sign=" + signature;
        requestUrl += "&timestamp=" + ts;
        requestUrl += "&version=202309";

        // Send HTTPS POST request
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Print response
            System.out.println("Response: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String calSign(String secret, long timestamp) {
        Map<String, String> queryParam = new TreeMap<>(); // Using TreeMap for sorting keys
        queryParam.put("app_key", "6buinkjt3hmld");
        queryParam.put("timestamp", String.valueOf(timestamp));
        queryParam.put("version", "202309");

        // Remove "sign" and "access_token" from query parameters
        queryParam.remove("sign");
        queryParam.remove("access_token");

        // Sort query parameters alphabetically
        StringBuilder signstring = new StringBuilder(secret);
        for (Map.Entry<String, String> entry : queryParam.entrySet()) {
            signstring.append(entry.getKey()).append(entry.getValue());
        }

        // Append secret again
        signstring.append(secret);

        // Calculate HMAC SHA256
        String sign = "";
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hmacBytes = hmacSha256.doFinal(signstring.toString().getBytes(StandardCharsets.UTF_8));
            sign = bytesToHex(hmacBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sign;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
