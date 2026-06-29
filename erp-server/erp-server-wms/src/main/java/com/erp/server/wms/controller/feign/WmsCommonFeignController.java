package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.EnumCacheUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/common")
public class WmsCommonFeignController extends BaseController {

    /**
     * 获取枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param type
     * @return
     */
    @GetMapping("enumDropDown")
    public ApiResult<List<Map<String,Object>>> enumSelect(@RequestParam(value = "type")String type) {
        Map<String,List<Map<String,Object>>> enumMaps = EnumCacheUtils.getInstance().getData();
        return success(enumMaps.get(type));
    }


}