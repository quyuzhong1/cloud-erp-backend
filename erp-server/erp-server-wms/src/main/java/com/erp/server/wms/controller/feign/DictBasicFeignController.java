package com.erp.server.wms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.server.wms.service.DictBasicService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 字典 Feign Controller
 * 对外提供以字典 id 集合 + type 查询字典 value 的能力，
 * 替代调用方使用 FeignQuery 跨服务直查 DictBasicEntity 的写法，
 * 以维护 WMS 模块的服务边界。
 *
 * @author wtr
 * @since 2026-06-04
 */
@RestController
@RequestMapping("/feign/dict")
public class DictBasicFeignController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 根据字典类型和 id 集合批量获取 id -> value 映射
     * @param type 字典 type，例如 warehouseType
     * @param ids  字典 id 集合
     */
    @PostMapping("/listValueMapByTypeAndIds")
    public Map<String, String> listValueMapByTypeAndIds(@RequestParam("type") String type,
                                                        @RequestBody List<String> ids) {
        return dictBasicService.listValueMapByTypeAndIds(type, ids);
    }
}
