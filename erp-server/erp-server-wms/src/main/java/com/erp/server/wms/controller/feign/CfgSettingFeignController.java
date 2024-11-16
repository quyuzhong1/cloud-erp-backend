package com.erp.server.wms.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.server.wms.service.CfgSettingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 加工单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/feign/cfgSetting")
public class CfgSettingFeignController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;


    @PostMapping("/getByKey")
    public CfgSettingEntity getByKey(@RequestBody String key) {
        return cfgSettingService.getByKey(key);
    }
}
