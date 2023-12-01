package com.erp.sdk.oms.amz.spapi.utils;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.AuthorizeDTO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.sdk.oms.amz.spapi.dto.AmazonTokenDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


/**
 * 亚马逊SP-API auth授权相关
 *
 * @author Jim
 * @date 2023/11/30
 */
@Slf4j
@Component
public class AmazonAuthClientUtils {

    private static final String GRANT_TYPE = "authorization_code";

    private static final String REFRESH_TOKEN = "refresh_token";

    /**
     * 获取到店铺的授权信息
     */
    public static AmazonTokenDTO getShopAuthorizeInfo(
            String url,
            String clientId,
            String clientSecret,
            String redirect_uri,
            String code
    ) {
        if (StringUtils.isBlank(url)
                || StringUtils.isBlank(clientId)
                || StringUtils.isBlank(clientSecret)
                || StringUtils.isBlank(redirect_uri)
                || StringUtils.isBlank(code)
        ){
            String msg = StrUtil.format("亚马逊授权参数异常,异常：存在空值,url={}, clientId={}, clientSecret={}, redirect_uri={}, code={}",
                    url, clientId, clientSecret, redirect_uri, code );
            throw new ServiceException(msg);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", GRANT_TYPE);
        params.put("redirect_uri", redirect_uri);
        params.put("code", code);

        String body;
        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .form(params)
                .execute()) {
            body = response.body();
        }
        log.debug("亚马逊获取授权结果:{}", body);
//        String body = "{\"access_token\":\"Atza|IwEBIKC-nZq0hqQG0lU-pKps7mdRwtxEWxXrBieXxZrfM0NI91L3cF_o5SnjjoKimsmJPNI2VQMcCkUozJq-QatnQlvOZQi6T_gC0fTanCj8hUjrDqKxW2iel2gxhMwwsY2vIEmlUv3JzMNa-zjf5YIoh0m61AC8uWan-voXO0TU0tkg7MtFxu4Ze8OU-qBKiXHGmrI4VfF7cXPFQ2_0uYgqm4NjWYGUbsyIR1rlV1CMBdiHCrn_y0Y1zZaxt9apXFLAGp2sIsQtaEdOA-zd0pageVnLzrC9X4Tgw3WYPgSgmZB6LJM2d8mGndWQoCO9v8HM-u59Rse367sM_aii2Ff689lfQLr8EM3otD6ETXQfXzwqqg\",\"refresh_token\":\"Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk\",\"token_type\":\"bearer\",\"expires_in\":3600}";
        JSONObject jsonObject = new JSONObject(body);
        String error = jsonObject.getStr("error");
        if (StringUtils.isNotBlank(error)) {
            String errorDescription = jsonObject.getStr("error_description");
            String msg = StrUtil.format("亚马逊授权异常：错误信息={}， 错误描述={}", error, errorDescription);
            throw new ServiceException(msg);
        }
        return JSONUtil.toBean(body, AmazonTokenDTO.class);
    }

    /**
     * 刷新店铺的授权信息
     */
    public static JSONObject refreshAuthorizeInfo(
            String url,
            String clientId,
            String clientSecret,
            String refreshToken
    ) {
        if (StringUtils.isBlank(url)
                || StringUtils.isBlank(clientId)
                || StringUtils.isBlank(clientSecret)
                || StringUtils.isBlank(refreshToken)
        ){
            String msg = StrUtil.format("亚马逊刷新授权信息参数异常,异常：存在空值,url={}, clientId={}, clientSecret={}, refreshToken={}",
                    url, clientId, clientSecret, refreshToken);
            throw new ServiceException(msg);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("refresh_token", refreshToken);
        params.put("grant_type", REFRESH_TOKEN);

        String body;
        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .form(params)
                .execute()) {
            body = response.body();
        }
        log.debug("亚马逊刷新授权结果:{}", body);
        JSONObject jsonObject = new JSONObject(body);
        String error = jsonObject.getStr("error");
        if (StringUtils.isNotBlank(error)) {
            String errorDescription = jsonObject.getStr("error_description");
            String msg = StrUtil.format("亚马逊刷新授权异常：错误信息={}， 错误描述={}", error, errorDescription);
            throw new ServiceException(msg);
        }
        return jsonObject;
    }


    public static void main(String[] args) {
        String clientId = "amzn1.application-oa2-client.aa03ca5c8fd741a49df6e35aac3c3287";
        String clientSecret = "amzn1.oa2-cs.v1.d9a5911d1548c33ed5b7fd7ede1f2932ef3041fae9f1cd973a32bf6b07b12183";
        String refresh_token = "Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk";
        String url = "https://api.amazon.com/auth/o2/token";
        JSONObject jsonObject = refreshAuthorizeInfo(url, clientId, clientSecret, refresh_token);
        System.out.println(jsonObject);
        // {"access_token":"Atza|IwEBIKC-nZq0hqQG0lU-pKps7mdRwtxEWxXrBieXxZrfM0NI91L3cF_o5SnjjoKimsmJPNI2VQMcCkUozJq-QatnQlvOZQi6T_gC0fTanCj8hUjrDqKxW2iel2gxhMwwsY2vIEmlUv3JzMNa-zjf5YIoh0m61AC8uWan-voXO0TU0tkg7MtFxu4Ze8OU-qBKiXHGmrI4VfF7cXPFQ2_0uYgqm4NjWYGUbsyIR1rlV1CMBdiHCrn_y0Y1zZaxt9apXFLAGp2sIsQtaEdOA-zd0pageVnLzrC9X4Tgw3WYPgSgmZB6LJM2d8mGndWQoCO9v8HM-u59Rse367sM_aii2Ff689lfQLr8EM3otD6ETXQfXzwqqg","refresh_token":"Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk","token_type":"bearer","expires_in":3600}
        /// {"access_token":"Atza|IwEBIBNH2Q3YxtjrAqr5ccUEDNKKt_qZpbR195bYb-AowjCL8kaTYw_civc2vLx900_ErdfCEUns2F6UpFem50J6rgRjkoiWw2MeUUB-nR_rg7TCpw25PDBiRnOp0TJ8aYyRgy7qUGf3QGsbp9DvDxK8DWg_Wfwfez3dZdXaZeFVe3Gb7HXqifFPudR18EckvUT65tp3pRCqP1TktUnf2_Jm50bvflSn-1KIgor3y0qIZj0YkPZn6qOLtKQsst380ae7WNhuIZEa0TencCocnhikql_KhSGme6ma8-EHN-jgTdEfFAxJrdd-SzkcScB4iDcGmTlLB5UvJJHJ0pokP7JDSU-Y2kZnl2pr09lzxFiwIaG_Og","refresh_token":"Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk","token_type":"bearer","expires_in":3600}
    }


    public static void main1(String[] args) {
        String clientId = "amzn1.application-oa2-client.aa03ca5c8fd741a49df6e35aac3c3287";
        String clientSecret = "amzn1.oa2-cs.v1.d9a5911d1548c33ed5b7fd7ede1f2932ef3041fae9f1cd973a32bf6b07b12183";
        String grant_type = "authorization_code";
        String redirect_uri = "http://tjuy5m.natappfree.cc:8060/store-permission-result";
        String code = "RHTzwIsVrrLkpfEVzKqs";
        String url = "https://api.amazon.com/auth/o2/token";
        AmazonTokenDTO shopAuthorizeInfo = getShopAuthorizeInfo(url, clientId, clientSecret, redirect_uri, code);
        System.out.println(shopAuthorizeInfo);
        // {"access_token":"Atza|IwEBINu999BVD2NsjzI20t7sr8URgNFINGy6NjEWZO5V6o7fkBGreuSV-MpXkD7ZicmLosmaCqOyNxiP_KErz4ZlHQAEuUHxecwoL01mnFY7PfcdXx_EdRs_ra0GSSUhGHttKBLlVVvbZvYaIq-Bewo3XTS-2G29UvqmNDZTx96hHXeimfGMp53WAxgChcyamtaMsIZAahFH9R0cwd4DTrT0hIANNIanxZXvPsheEc6x307uUzEczkgilIwfY-BAfTlvlADBEbuK6MocbKqWyg0DhgGSm0kO5fEzJrvuUWvSn3Fr1Sm7lIIJ0DD4bjcoXxSIgq4","refresh_token":"Atzr|IwEBIIAk0ZC6REzfuMKktAqsxNSwMAKUhtU60y0BV0BDBwhK70zqmX_10F_xz9xV-G2dosTUdhK0GJvdSDGNcnHO50t_9bYqulua1PdhNDSQsNnTXd9xpWeTbYhZfzdmQSy3UgmG1S0kKpOhy2XFyC1f93Tuq-uminDGYrqvitG1bbqqB9nBfd7eiK2csTVqrxpxLlmM49xm8a7iTrC8o7F7nmf3mDSz4biyE4qQZnIDd1oEv0q_rEz0QFXxm80cs8CcvLj15CJgOG-R7C5SfbaDXDaLFigamEEVp5JS5FAy7v6E8PUNkLQstrVwlxO9XEuQ7CNsX0aRy08jRkH01eb2Pamk","token_type":"bearer","expires_in":3600}
    }


    public static void main2(String[] args) {
        String sellerCentralUrl = AmazonMarketplaceEnum.AE.getSellerCentralUrl();
        String authUrl = sellerCentralUrl.concat("/apps/authorize/consent?application_id=amzn1.sp.solution.22d664b5-10f1-4c7a-b020-43a7ebccca3b");
        System.out.println(authUrl);
        // 生成随机数据
        SecureRandom secureRandom = new SecureRandom();
        // 生成 256 字节的随机数据
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        // 进行 Base64 编码
        String state = Base64.getEncoder().encodeToString(randomBytes);
        System.out.println(state);
        String concat = authUrl.concat("&state=" + state);
        System.out.println(concat);
    }
}
