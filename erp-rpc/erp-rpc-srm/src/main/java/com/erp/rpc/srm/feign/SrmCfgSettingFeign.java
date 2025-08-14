package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.vo.SupplierConfigVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zdy
 * @ClassName SrmCfgSettingFeign
 * @description: TODO
 * @date 2024年01月11日
 * @version: 1.0
 */
@FeignClient(name = "erp-srm", contextId = "srmCfgSettingFeign",configuration = {FeignErrorDecoder.class})
public interface SrmCfgSettingFeign {

    @PostMapping("/feign/cfgSetting/getConfigList")
    List<SupplierConfigVO> getConfigList(@RequestBody List<String> supplierIds);

    /**
     * 根据key和供应商id查询配置
     * @Author Luo_WG
     * @Date 2024/1/12 14:16
     * @param supplierIds
     * @param key
     * @return java.util.List<com.erp.model.srm.entity.CfgSettingEntity>
     **/
    @PostMapping("/feign/cfgSetting/listByKeyAndSupplier")
    List<CfgSettingEntity> listByKeyAndSupplier(@RequestParam("key") String key, @RequestParam("supplierIds") List<String> supplierIds);

    /**
     * @description: 根据key值查询所有供应商配置信息
     * @author Will
     * @date: 2024/1/17 10:47
     * @param key
     * @return List<ViewDTO>
     */
    @PostMapping("/feign/cfgSetting/listByKey")
    List<CfgSettingDTO.ViewDTO> listByKey(@RequestBody String key);

}
