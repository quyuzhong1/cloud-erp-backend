package com.erp.server.dmp.controller.open;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.Md5Util;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.sdk.third.kingdee.utils.GoodCangApiUtils;
import com.erp.server.dmp.pull.service.GoodcangStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 亚马逊SP-APi
 *
 * @Author Jim
 * @Date 2023/3/29 11:11
 **/
@Slf4j
@RestController
@RequestMapping("open/api/amz")
public class AmazonSpApiOpenController {


    /**
     * 通知回调
     */
    @PostMapping("/notifications")
    public ApiResult<?> notifications(@RequestBody Map<String, String> params) {
        log.warn("亚马逊SP-APi通知回调>>>subscribe>>>dto ={}", params);
        return ApiResult.success();
    }

}
