package com.erp.server.srm.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.server.srm.service.CfgSettingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName SrmCfgSettingFeignController
 * @description: srm 配置
 * @date 2024年01月11日
 * @version: 1.0
 */
@RestController
@RequestMapping("/feign/cfgSetting")
public class SrmCfgSettingFeignController extends BaseController {
    @Resource
    private CfgSettingService cfgSettingService;
    /**
     * 获取供应商订单配置信息
     *
     * @return
     */
    @PostMapping("/getConfigList")
    public List<SupplierConfigVO> getConfigList(@RequestBody List<String> supplierIds) {
        return cfgSettingService.getConfigList(supplierIds);
    }

    /**
     * 根据key和供应商id查询配置
     * @Author Luo_WG
     * @Date 2024/1/12 14:16
     * @param supplierIds
     * @param key
     * @return java.util.List<com.erp.model.srm.entity.CfgSettingEntity>
     **/
    @PostMapping("/listByKeyAndSupplier")
    public List<CfgSettingEntity> listByKeyAndSupplier(@RequestParam("key") String key, @RequestParam("supplierIds") List<String> supplierIds) {
        return cfgSettingService.listByKeyAndSupplier(key, supplierIds);
    }

    /**
     * @description: 根据key值查询所有供应商配置信息
     * @author Will
     * @date: 2024/1/17 10:49
     * @param key
     * @return List<ViewDTO>
     */
    @PostMapping("/listByKey")
    public List<CfgSettingDTO.ViewDTO> listByKey(@RequestBody String key) {
        return cfgSettingService.listByKey(key);
    }
}
