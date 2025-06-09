package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.CfgSettingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: 系统配置远程调用
 * @author Will
 * @date: 2024/1/19 9:20
 */
@FeignClient(name = "erp-wms", contextId = "wmsCfgSetting" ,configuration = {FeignErrorDecoder.class})
public interface CfgSettingFeign {

    /**
     * @description: 根据唯一值查询系统配置
     * @author Will
     * @date: 2024/1/19 9:24
     * @param key
     * @return CfgSettingEntity
     */
    @PostMapping("feign/cfgSetting/getByKey")
    CfgSettingEntity getByKey(@RequestBody String key);


}
