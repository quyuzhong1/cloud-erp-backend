import cn.hutool.json.JSONUtil;
import com.sdk.oms.wildberries.constant.WildberriesConstant;
import com.sdk.oms.wildberries.dto.*;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author zdy
 * @ClassName WildberriesSDKServiceTest
 * @description: TODO
 * @date 2025年09月19日
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={WildberriesSDKService.class})
public class WildberriesSDKServiceTest {

    @Resource
    private WildberriesSDKService wildberriesSDKService;
    @Test
    public void shopCheck() {
        WildberriesResponse response = wildberriesSDKService.checkToken(WildberriesConstant.TOKEN);
        System.out.println(JSONUtil.toJsonStr(response));
    }


    @Test
    public void getSkuList() {
        SkuRequest skuRequest = SkuRequest.builder()
                .settings(SkuRequest.Setting.builder()
                        .cursor(SkuRequest.Cursor.builder().limit(1).build())
                        .filter(SkuRequest.Filter.builder().withPhoto(-1).build())
                        .sort(SkuRequest.Sort.builder().ascending(Boolean.FALSE).build())
                        .build())
                .build();
        System.out.println(JSONUtil.toJsonStr(skuRequest));
        SkuResponse response = wildberriesSDKService.getSkuList(WildberriesConstant.TOKEN, skuRequest);
        System.out.println(response);
        System.out.println(response.isSuccess());
        System.out.println(response.getMsg());
    }
    @Test
    public void getOrderList() {
        OrderRequest orderRequest = OrderRequest.builder().build();
        System.out.println(JSONUtil.toJsonStr(orderRequest));
        OrderResponse response = wildberriesSDKService.getOrderList(WildberriesConstant.TOKEN, orderRequest);
        System.out.println(response);
        System.out.println(response.isSuccess());
        System.out.println(response.getMsg());
    }
}
