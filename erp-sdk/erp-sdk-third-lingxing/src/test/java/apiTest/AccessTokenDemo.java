package apiTest;


import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.dto.Token;
import com.sdk.third.lingxing.utils.AKRestClientBuild;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessTokenDemo {

    /**
     * <p>
     *     这个demo之合适 参数是以query-param形式传参的，如果有body形式传参参考orderDemo
     *     示例中的 appId，appSecret需要替换成客户自己申请的appId，appSecret endpoint
     * </p>
     */
    public static void main(String[] args) throws Exception {
        String endpoint = "xxxx";
        String appId = "xxxx";
        String appSecret = "xxxx";
        // 如果用postman等其他工具调试时，需要将appSecret用urlencode.encode()进行转义
        Result<Token> result = AKRestClientBuild.builder().endpoint(endpoint).getAccessToken(appId, appSecret);
        log.info("token:{}", result);
        // Result{, code=200, msg='OK', data={access_token=d77283df-f841-4fb0-926e-c81a8e672ce0, refresh_token=7aee55a3-fc4b-4c8c-bf86-bfd93eb2b31e, expires_in=7199}}
    }

}
