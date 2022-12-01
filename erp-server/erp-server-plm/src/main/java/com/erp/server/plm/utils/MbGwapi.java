package com.erp.server.plm.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.activation.MimetypesFileTypeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class MbGwapi {

    /**
     * API请求调用地址
     */
    private static String REQ_URL = "https://gwapi.mabangerp.com/api/v2";

    /**
     * 开发者编号
     */
    private static String API_KEY = "200780";

    /**
     * 开发者密钥
     */
    private static String API_TOKEN = "13c324fa18feaaeb0ebcc8a7746ebfca";

    /**
     * 程序入口
     * 
     * @param args
     */
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();

//        map.put("timeStart", "2022-10-16 14:42:33");
//        map.put("timeEnd", "2022-11-16 14:42:33");
//        map.put("page", "1");
//        map.put("pageSize", "100");
        String data = callGwapi("sys-get-shop-list", map);
        // String data = callGwapi2("sys-get-currency-rate-list", map);

        // 上传文件（1 本地文件存在 2 传参文件开头添加@ 3 调用callGwapi2方法）
        // map.put("mbFile", "@/data/1.jpg");
        // String data = callGwapi2("sys-upload-file", map);
        JSONObject object = JSONObject.parseObject(data);
      /*  JSON.
        String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        System.out.println(pretty);*/
        System.out.println(data);
    }

    /**
     * 调用API
     * 
     * @param api
     * @param reqParams
     * @return
     */
    public static String callGwapi(String api, Map<String, Object> reqParams) {
        // 创建httpclient
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(REQ_URL);
        // 封装传参数据
        Map<String, Object> datas = new HashMap<String, Object>();
        datas.put("api", api);
        datas.put("appkey", API_KEY);
        datas.put("data", reqParams);
        datas.put("version", 1);
        datas.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());

        // 将传参转为Json格式
        String jsonContent = JSONObject.toJSONString(datas);
        // 封装请求头
        String authorization = hmacSha256(jsonContent, API_TOKEN);
        System.out.println("jsonContent：" + jsonContent);
        System.out.println("Authorization：" + authorization);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", authorization);

        // 封装提交数据
        StringEntity se = new StringEntity(jsonContent, "UTF-8");
        httpPost.setEntity(se);

        String body = "";
        CloseableHttpResponse response;
        try {
            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            response.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }
    
    /**
     * 调用API
     * 
     * @param api
     * @param reqParams
     * @return
     */
   /* public static String callGwapi2(String api, Map<String, Object> reqParams) {
        Map<String, Object> textdatas = new HashMap<String, Object>();
        Map<String, String> filedatas = new HashMap<String, String>();
        List<String> filefields = new ArrayList<String>(); 
        // 解析传参中哪些是文件类型传参
        for(Map.Entry<String, Object> reqParam: reqParams.entrySet()) {
            if(reqParam.getValue() instanceof String && isPostFile(reqParam.getValue().toString())) {
                filedatas.put(reqParam.getKey(), reqParam.getValue().toString().substring(1));
                filefields.add(reqParam.getKey());
            } else {
                textdatas.put(reqParam.getKey(), reqParam.getValue());
            }
        }
        if(filefields.size() > 0) {
            textdatas.put("_filefields", String.join(",", filefields));
        }
        // 传参转为JSON
        String datajson = JSON.toJSONString(textdatas);
//      System.out.println(datajson);
        
        // 封装传参数据
        Map<String, String> reqdatas = new TreeMap<String, String>();
//      reqdatas.put("accesstoken", "xxx");
        reqdatas.put("api", api);
        reqdatas.put("appkey", API_KEY);
        reqdatas.put("data", datajson);
        reqdatas.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());

        // 传参转为JSON
        String reqjson = JSON.toJSONString(reqdatas);
        // System.out.println(reqjson);
        
        // 封装请求头
        String authorization = hmacSha256(reqjson, API_TOKEN);
        
        // 提交请求
        return formUpload(REQ_URL, reqdatas, filedatas, authorization);
    }
*/
    /**
     * hmac256加密
     * 
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] hmacSha256(String data, byte[] key) {
        String algorithm = "HmacSHA256";
        Mac sha256_HMAC;
        byte[] array = null;
        try {
            sha256_HMAC = Mac.getInstance(algorithm);
            sha256_HMAC.init(new SecretKeySpec(key, algorithm));
            array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return array;
    }

    /**
     * hmac256加密
     * 
     * @param data
     * @param key
     * @return
     */
    public static String hmacSha256(String data, String key) {
        byte[] array = null;
        try {
            array = hmacSha256(data, key.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toLowerCase();
    }
    
    /**
     * 是否为文件
     * @param text
     * @return
     */
    private static boolean isPostFile(String text) {
        if(! "@".equals(text.substring(0, 1))) {
            return false;
        }
        File file = new File(text.substring(1));
        if(! file.isFile()) {
            return false;
        }
        return true;
    }
    
    /**
     * 上传图片
     * @param urlStr
     * @param textMap
     * @param fileMap
     * @param contentType 没有传入文件类型默认采用application/octet-stream
     * contentType非空采用filename匹配默认的图片类型
     * @return 返回response数据
     */
    @SuppressWarnings("rawtypes")
    private static String formUpload(String requestUrl, Map<String, String> textMap,
            Map<String, String> fileMap, String authorization) {
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------0123456789"; 
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            // file
            if (fileMap != null) {
                Iterator iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();
                    
                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    String contentType = new MimetypesFileTypeMap().getContentType(file);
                    //contentType非空采用filename匹配默认的图片类型
                    if(!"".equals(contentType)){
                        if (filename.endsWith(".png")) {
                            contentType = "image/png"; 
                        }else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                            contentType = "image/jpeg";
                        }else if (filename.endsWith(".gif")) {
                            contentType = "image/gif";
                        }else if (filename.endsWith(".ico")) {
                            contentType = "image/image/x-icon";
                        }
                    }
                    if (contentType == null || "".equals(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }

}
