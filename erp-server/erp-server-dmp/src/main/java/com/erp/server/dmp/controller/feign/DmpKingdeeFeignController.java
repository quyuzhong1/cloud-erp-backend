package com.erp.server.dmp.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.push.service.kingdee.KingdeePushService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description: 远程调用控制层
 * @author Will
 * @date: 2023/1/11 17:46
 */
@RestController
@RequestMapping("dmp/kigdee/feign")
public class DmpKingdeeFeignController extends BaseController {

    @Resource(name = "kingdeeProductDetailService")
    private KingdeePushService kingdeeProductDetailService;


    /**
     * @description: 发送产品信息到金蝶
     * @author Will
     * @date: 2023/1/11 18:16
     * @param map
     */
    @PostMapping("/pushProductDetail")
    public void pushProductDetail(@RequestBody Map<String,Object> map) {
        kingdeeProductDetailService.pushKingdee(map);
    }

}
